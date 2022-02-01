import subprocess
import argparse
import os
import json
from src.main.python.train import train_gpt
from src.main.python.generate import generate_gpt
import gym
from gym import spaces
# from stable_baselines import DQN
import numpy as np
from stable_baselines3.common.env_checker import check_env
from stable_baselines3 import PPO

def makedir(name):
    dirname = './output/{}'.format(name)
    os.makedirs(dirname, exist_ok=True)
    return dirname

def override_default(current):
    assert current != None

class ProbabilisticSynthesizerEnv(gym.Env):
    """Custom Environment that follows gym interface"""
    metadata = {'render.modes': ['human']}

    def __init__(self, language, runname, evalname, num_train_gen, num_eval_gen, num_rules, num_run_types, attr_regex):
        super(ProbabilisticSynthesizerEnv, self).__init__()
        # Define action and observation space
        # They must be gym.spaces objects
        self.modeldir = makedir(runname)
        self.num_train_gen = num_train_gen
        self.num_eval_gen = num_eval_gen
        self.language = language
        self.synth_tmp_path = "{}/tmp_synth.txt".format(self.modeldir)
        self.gen_tmp_path = "{}/tmp_gen.txt".format(self.modeldir)
        self.results_tmp_path = "{}/tmp_results.txt".format(self.modeldir)
        self.eval_examples_path = "{}/gpt-generated-{}-eval.txt".format(makedir(evalname), evalname)
        self.evalname = evalname
        self.runname = runname
        self.attr_regex = attr_regex
        # Actions are a probability vector outputted, 1 for each NT-symbol.  
        self.action_space = spaces.Box(low=0, high=100, shape=(num_rules,), dtype=np.float32)
        # Using the frequencies of examples/errors as the observation space
        self.observation_space = spaces.Box(low=0, high=10000,
            shape=(num_run_types,), dtype=np.int16)

    def step(self, action):
        # Make training data
        synth_cmd = 'echo -n | ./gradlew run --args="generate --useful -n {} -o {} -l {}"'.format(self.num_train_gen, self.synth_tmp_path, self.language)
        subprocess.call(synth_cmd, shell=True)
        train_gpt(run_name = self.runname, generated_path = self.synth_tmp_path, output_dir = self.modeldir, attr_regex=self.attr_regex, use_pretrained=True)
        generate_gpt(model_run_name = self.runname, eval_output_generated_fname=self.gen_tmp_path, eval_generated_fname=self.eval_examples_path, model_dir_base = self.modeldir, num_attempts=self.num_eval_gen)
        eval_cmd = 'echo -n | ./gradlew run --args="evaluate -i {} -l {} -o {} -e {}"'.format(self.gen_tmp_path, self.language, self.results_tmp_path, "/dev/null")
        subprocess.call(eval_cmd, shell=True)
        eval_res = json.loads(open(self.results_tmp_path, "r").read())
        rrc = eval_res["runResultCounts"]
        obs_vec = np.array([
            rrc["SUCCESS"], 
            rrc["BAD"], 
            rrc["PARSEERROR"], 
            rrc["TYPEERROR"], 
            rrc["DECODEERROR"], 
            rrc["VERIFYERROR"], 
            rrc["NAMEERROR"], 
            rrc["RUNTIMEERROR"]
        ])
        info = dict() # Can add stuff to this but idk why except for maybe metrics/debugging?
        return obs_vec, eval_res["numFullyCorrectPrograms"], False, info
    def reset(self):
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
    parser.add_argument('--num_gen_per_iter', type=int, default=100, 
                        help='number of examples to make for GPT to train on each iteration')
    parser.add_argument('--num_attempts', type=int, default=1,
                        help='number of attempts GPT has to create a working program each iteration')
    parser.add_argument('--num_iter', type=int, default=50,
                        help='number of iterations of RL to run')
    parser.add_argument('--attr_regex', type=str, default=None,
                        help='If using a CFG-printing language, this is an attribute regex to filter the attributes that GPT sees. ')
    parser.add_argument('--randomize_weights', action='store_true', help="Use randomized, as opposed to pretrained EutherAI weights when training. ")
    args = parser.parse_args()
    language = args.language
    runname = args.runname
    num_gen_per_iter = args.num_gen_per_iter
    evalname = args.evalname
    lang_data_tmp_file_path = "tmp.txt"
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
        num_run_types=8,
        attr_regex=args.attr_regex
    )
    model = PPO("MlpPolicy", rl_env, verbose=1)
    model.learn(total_timesteps=args.num_iter)
    model.save(runname)

    

if __name__ == "__main__": 
    main()
