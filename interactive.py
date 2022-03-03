import subprocess
import argparse
import os
import json
import transformers
from src.main.python.utils import RedirectStdStreams
from src.main.python.train import train_gpt
from src.main.python.generate import generate_gpt
from stable_baselines3.common.callbacks import BaseCallback
import gym
from gym import spaces
# from stable_baselines import DQN
import numpy as np
from stable_baselines3.common.env_checker import check_env
from stable_baselines3 import PPO
from stable_baselines3.common.vec_env.dummy_vec_env import DummyVecEnv

def makedir(name):
    dirname = './output/{}'.format(name)
    os.makedirs(dirname, exist_ok=True)
    return dirname

def override_default(current):
    assert current != None

def make_gen_config(probs_vec, num_attempts, max_depth):
    to_serial = dict()
    to_serial["orderedWeights"] = probs_vec.tolist()
    to_serial["numRandomTries"] = num_attempts
    to_serial["maxProgramDepth"] = max_depth
    return json.dumps(to_serial)    

def count_examples(eval_file_path):
    eval_f = open(eval_file_path, "r")
    lines = eval_f.readlines()
    return len(list(filter(lambda line : line.strip() == "Inputs:", lines)))
class TensorboardCallback(BaseCallback):
    """
    Custom callback for plotting additional values in tensorboard.
    """

    def __init__(self, verbose=0):
        super(TensorboardCallback, self).__init__(verbose)
    def get_unwrapped_env(self) -> gym.Env:
        return self.training_env.envs[0]
    def _on_step(self) -> bool:
        for run_type in self.get_unwrapped_env().run_types:
            self.logger.record('{}_count'.format(run_type), self.get_unwrapped_env().last_rrcs[run_type])
        return True

class ProbabilisticSynthesizerEnv(gym.Env):
    """Custom Environment that follows gym interface"""
    metadata = {'render.modes': ['human']}

    def __init__(self, language, runname, evalname, num_train_gen, num_eval_gen, num_rules, run_types, num_steps, attr_regex):
        super(ProbabilisticSynthesizerEnv, self).__init__()
        # Define action and observation space
        # They must be gym.spaces objects
        self.modeldir = makedir(runname)
        self.run_types = run_types
        self.num_train_gen = num_train_gen
        self.num_eval_gen = num_eval_gen
        self.language = language
        self.log_path = "{}/inner_log.txt".format(self.modeldir)
        self.log_f = open(self.log_path, "w+")
        self.synth_tmp_path = "{}/tmp_synth.txt".format(self.modeldir)
        self.gen_tmp_path = "{}/tmp_gen.txt".format(self.modeldir)
        self.results_tmp_path = "{}/tmp_results.txt".format(self.modeldir)
        self.eval_examples_path = "{}/gpt-generated-{}-eval.txt".format(makedir(evalname), evalname)
        self.results_detailed_tmp_path = "{}/results_detailed.txt".format(self.modeldir)
        self.evalname = evalname
        self.runname = runname
        self.config_location = "{}/generation_config.txt".format(self.modeldir)
        self.attr_regex = attr_regex
        # Actions are a probability vector outputted, 1 for each NT-symbol.  
        self.action_space = spaces.Box(low=0.00001, high=1, shape=(num_rules,), dtype=np.float32)
        # Using the frequencies of examples/errors as the observation space
        self.observation_space = spaces.Box(low=0, high=1,
        shape=(count_examples(self.eval_examples_path), len(run_types)), dtype=np.float32)
        self.step_num = 0
        self.max_num_steps = num_steps
    def step(self, action):
        print("Taking step!")
        self.step_num += 1
        # Let this stuff go into the log file, to seperate it from the logs of StableBaselines
        with RedirectStdStreams(stdout=self.log_f, stderr=self.log_f):
            # Make training data
            # First, normalize the probability vector
            normalized_prob_vec = action / np.sum(action)
            # Make the config json and pass it to the synthesizer
            config_str = make_gen_config(normalized_prob_vec, 5, 5)
            open(self.config_location, "w").write(config_str)
            synth_cmd = 'echo -n | ./gradlew run --args="generate --useful -n {} -o {} -l {} -g {}"'.format(self.num_train_gen, self.synth_tmp_path, self.language, self.config_location)
            subprocess.call(synth_cmd, shell=True, stdout=self.log_f, stderr=self.log_f)
            # Train gpt on the new batch
            train_gpt(run_name = self.runname, generated_path = self.synth_tmp_path, output_dir = self.modeldir, attr_regex=self.attr_regex, use_pretrained=True, use_saved=True)
            # And then evaluate the model by using it
            generate_gpt(model_run_name = self.runname, eval_output_generated_fname=self.gen_tmp_path, eval_generated_fname=self.eval_examples_path, model_dir_base = self.modeldir, num_attempts=self.num_eval_gen)
            eval_cmd = 'echo -n | ./gradlew run --args="evaluate -i {} -l {} -o {} -e {}"'.format(self.gen_tmp_path, self.language, self.results_tmp_path, self.results_detailed_tmp_path)
            subprocess.call(eval_cmd, shell=True, stdout=self.log_f, stderr=self.log_f)
            # Turn the evaluation results into a vector so we can do RL with it. 
            eval_res = json.loads(open(self.results_tmp_path, "r").read())
            rrc = eval_res["runResultCounts"]
            run_results = eval_res["runResults"]
            run_results_indexed = np.array([self.run_types.index(restype) for restype in run_results])
            num_fully_correct = eval_res["numFullyCorrectPrograms"]
            run_type_counts_vec = np.array([rrc[runtype] for runtype in self.run_types])
            run_types_onehot_vec = np.zeros((run_results_indexed.size, len(self.run_types)), dtype=np.float32)
            run_types_onehot_vec[np.arange(run_results_indexed.size), run_results_indexed] = 1
            obs_vec = run_types_onehot_vec
            # A general rule: Success should be the best, and then bad results, and then runtime errors. 
            # This is because the type/decode/verify/name errors shouldn't really every occur
            # And we prefer correct programs to bad ones, but bad programs to non-running ones. 
            # Also, the weight for a fully correct program should be >10x the success weight, because then it might
            # Prioritize getting mostly successes instead of ALL successes. 
            runtime_reward_weights = np.array([
                10,
                2,
                0,
                0,
                0, 
                0, 
                0, 
                1
            ])
            runtime_rewards = np.dot(run_type_counts_vec, runtime_reward_weights)
            self.last_rrcs = rrc
            info = dict() # Can add stuff to this but idk why except for maybe metrics/debugging?
        done = self.step_num >= self.max_num_steps
        # What's a good reward function? Idk, tbh. 
        return obs_vec, (num_fully_correct * 100) + runtime_rewards, done, info
    def reset(self):
        self.step_num = 0
        return np.zeros(8)  # reward, done, info can't be included
    def close (self):
        pass
    
def main():
    parser = argparse.ArgumentParser(description='Run the entire generate-train-generate-eval pipeline')
    parser.add_argument('--runname', type=str,
                        help='name of the run', required=True)
    parser.add_argument('--evalname', type=str, help='name of the evaluation set.', required=True)
    parser.add_argument('--language', type=str,
                        help='Name of the language to eval on (deepcoder or lambda2)', required=True)
    parser.add_argument('--num_gen_per_iter', type=int, default=1000, 
                        help='number of examples to make for GPT to train on each iteration')
    parser.add_argument('--num_attempts', type=int, default=1,
                        help='number of attempts GPT has to create a working program each iteration')
    parser.add_argument('--num_iter', type=int, default=10,
                        help='number of iterations of RL to run')
    parser.add_argument('--attr_regex', type=str, default=None,
                        help='If using a CFG-printing language, this is an attribute regex to filter the attributes that GPT sees. ')
    parser.add_argument('--randomize_weights', action='store_true', help="Use randomized, as opposed to pretrained EutherAI weights when training. ")
    args = parser.parse_args()
    language = args.language
    runname = args.runname
    num_gen_per_iter = args.num_gen_per_iter
    evalname = args.evalname
    transformers.logging.set_verbosity_error()
    lang_data_tmp_file_path = "{}/language_metadata.txt".format(makedir(runname))
    lang_data_cmd = 'echo -n | ./gradlew run --args="metadata -l {} -o {}"'.format(language, lang_data_tmp_file_path)
    subprocess.call(lang_data_cmd, shell=True)
    lang_data_tmp_file = open (lang_data_tmp_file_path, "r")
    lang_data = json.loads(lang_data_tmp_file.read())
    num_rules = len(lang_data["rules"])
    rl_env = ProbabilisticSynthesizerEnv(
        language=language, 
        runname=runname, 
        evalname=evalname,
        num_train_gen=num_gen_per_iter,
        num_eval_gen=args.num_attempts,
        num_rules=num_rules,
        run_types=["SUCCESS", "BAD", "PARSEERROR", "TYPEERROR", "DECODEERROR", "VERIFYERROR", "NAMEERROR", "RUNTIMEERROR"],
        num_steps=args.num_iter,
        attr_regex=args.attr_regex
    )
    model = PPO("MlpPolicy", rl_env, tensorboard_log="./rl-logs/", verbose=1, n_steps=2, batch_size=2, n_epochs=1)
    model.learn(total_timesteps=args.num_iter, callback=TensorboardCallback(verbose=1), n_eval_episodes=0)
    model.save("{}/saved-model".format(makedir(runname)))
    print("Finished RL loop!")

    

if __name__ == "__main__": 
    main()
