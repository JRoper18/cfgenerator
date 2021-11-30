import subprocess
import argparse
import os


def makedir(name):
    dirname = './output/{}'.format(name)
    os.makedirs(dirname, exist_ok=True)
    return dirname

def override_default(current):
    assert current != None


def main():
    parser = argparse.ArgumentParser(description='Run the entire generate-train-generate-eval pipeline')
    parser.add_argument('--genname', type=str,
                        help='name of the run of the CFG generation (changes file suffixes)')
    parser.add_argument('--evalname', type=str,
                        help='name of the run of the evaluation (changes file suffixes)')
    parser.add_argument('--modelname', type=str,
                        help='name of the run of the trained model (changes file suffixes)')
    parser.add_argument('--allname', type=str,
                        help='name of everything. cant be used with other name args. ')

    parser.add_argument('--language', type=str,
                        help='Name of the language to eval on (deepcoder or lambda2)')
    parser.add_argument('--do_all', action='store_true',
                        help = 'Do the entire pipeline. ')
    parser.add_argument('--do_cfgs', action='store_true',
                        help = 'Generate training examples from our CFG generator to train GPT on. ')
    parser.add_argument('--do_train', action='store_true',
                        help = 'Train GPT on CFG-generated programs')
    parser.add_argument('--do_eval_cfgs', action='store_true',
                        help = 'Generate evaluation examples from our CFG generator to evaluate GPT on. ')
    parser.add_argument('--do_gpt_gen', action='store_true',
                        help = 'Generate programs using a trained GPT model')
    parser.add_argument('--do_eval', action='store_true',
                        help = 'Evaluate examples created by GPT. ')
    parser.add_argument('--num_train', type=int, default=10000, 
                        help='number of examples to make for GPT to train on')
    parser.add_argument('--num_eval', type=int, default=1000, 
                        help='number of examples to make for GPT to generate/eval for')
    parser.add_argument('--attr_regex', type=str, default=None,
                        help='If using a CFG-printing language, this is an attribute regex to filter the attributes that GPT sees. ')
    parser.add_argument('--randomize_weights', action='store_true', help="Use randomized, as opposed to pretrained EutherAI weights when training. ")
    args = parser.parse_args()

    language = args.language
    modelname = args.modelname
    evalname = args.evalname
    genname = args.genname
    allname = args.allname
    if allname != None:
        assert modelname is None
        assert genname is None
        assert evalname is None
        modelname = allname
        genname = allname
        evalname = allname
    gendir = makedir(genname)
    modeldir = makedir(modelname)
    evaldir = makedir(evalname)
    do_all = args.do_all
    attr_regex = args.attr_regex
    cfg_generated_train_path = '{}/cfg-generated-{}.txt'.format(gendir, genname)
    cfg_generated_eval_path = '{}/cfg-generated-{}-eval.txt'.format(evaldir, evalname)
    gpt_generated_eval_path = '{}/gpt-generated-{}-model-{}-eval.txt'.format(evaldir, evalname, modelname)
    eval_log_path = '{}/{}-model-{}-results.txt'.format(evaldir, evalname, modelname)
    examples_eval_path = '{}/{}-model-{}-examples.txt'.format(evaldir, evalname, modelname)
    if(args.do_cfgs or do_all):
        cmd = 'echo -n | ./gradlew run --args="generate --useful -n {} -o {} -l {}"'.format(args.num_train, cfg_generated_train_path, language)
        print(cmd)
        ret = subprocess.call(cmd, shell=True)
        if (ret != 0):
            return
    if(args.do_train or do_all):
        from src.main.python.train import train_gpt
        train_gpt(run_name = modelname, generated_path = cfg_generated_train_path, output_dir = modeldir, attr_regex=attr_regex, use_pretrained=(not args.randomize_weights))

    if(args.do_eval_cfgs or do_all):
        cmd = 'echo -n | ./gradlew run --args="generate --useful -n {} -o {} -l {}"'.format(args.num_eval, cfg_generated_eval_path, language)
        ret = subprocess.call(cmd, shell=True)
        if (ret != 0):
            return

    if(args.do_gpt_gen or do_all):
        from src.main.python.generate import generate_gpt
        generate_gpt(model_run_name = modelname, eval_output_generated_fname=gpt_generated_eval_path, eval_generated_fname=cfg_generated_eval_path, model_dir_base = modeldir)

    if(args.do_eval or do_all):
        cmd = 'echo -n | ./gradlew run --args="evaluate -i {} -l {} -o {} -e {}"'.format(gpt_generated_eval_path, language, eval_log_path, examples_eval_path)
        ret = subprocess.call(cmd, shell=True)
        if (ret != 0):
            return
    

if __name__ == "__main__": 
    main()
