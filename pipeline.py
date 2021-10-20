import subprocess
import argparse
from src.main.notebooks.train import train_gpt
from src.main.notebooks.generate import generate_gpt

def main():
    parser = argparse.ArgumentParser(description='Run the entire generate-train-generate-eval pipeline')
    parser.add_argument('--evalname', type=str,
                        help='name of the run of the evaluation (changes file suffixes)')
    parser.add_argument('--modelname', type=str,
                        help='name of the run of the trained model (changes file suffixes)')

    parser.add_argument('--language', type=str,
                        help='Name of the language to eval on (deepcoder or lambda2)')
    parser.add_argument('--do_cfgs', action='store_true')
    parser.add_argument('--do_train', action='store_true')
    parser.add_argument('--do_eval_cfgs', action='store_true')
    parser.add_argument('--do_eval', action='store_true')
    parser.add_argument('--num_train', type=int, default=10000, 
                        help='number of examples to make for GPT to train on')
    parser.add_argument('--num_eval', type=int, default=1000, 
                        help='number of examples to make for GPT to generate/eval for')
    
    args = parser.parse_args()

    language = args.language
    modelname = args.modelname
    evalname = args.evalname
    cfg_generated_train_path = './output/generated-{}.txt'.format(modelname)
    cfg_generated_eval_path = './output/generated-{}.txt'.format(evalname)
    if(args.do_cfgs):
        cmd = 'echo -n | ./gradlew run --args="generate --useful -n {} -o {} -l {}"'.format(args.num_train, cfg_generated_train_path, language)
        print(cmd)
        ret = subprocess.call(cmd, shell=True)
        if (ret != 0):
            return
    if(args.do_train):
        train_gpt(run_name = modelname, generated_path = cfg_generated_train_path)

    if(args.do_eval_cfgs):
        cmd = 'echo -n | ./gradlew run --args="generate --useful -n {} -o {} -l {}"'.format(args.num_eval, cfg_generated_eval_path, language)
        ret = subprocess.call(cmd, shell=True)
        if (ret != 0):
            return

    if(args.do_eval):
        generate_gpt(model_run_name = modelname, eval_run_name = evalname)
        cmd = 'echo -n | ./gradlew run --args="evaluate -i ./output/gpt-eval-{}.txt -l {}"'.format(evalname, language)
        ret = subprocess.call(cmd, shell=True)
        if (ret != 0):
            return
    

if __name__ == "__main__": 
    main()
