import torch
from transformers import GPTNeoForCausalLM, GPT2Tokenizer
from .utils import ProgramDataset, make_max_length


def generate_gpt(param_size = "125M",
    eval_run_name = "5len2",
    model_run_name = "3len"
):
    pretrained_name = "EleutherAI/gpt-neo-%s" % param_size
    model_dir = "./results-%s-%s" % (param_size, model_run_name)
    output_dir = "./results-%s-%s" % (param_size, eval_run_name)


    # Now, make the outputs for us to evaluate:
    fine_model = GPTNeoForCausalLM.from_pretrained(model_dir).cuda()
    fine_tokenizer = GPT2Tokenizer.from_pretrained(model_dir, 
        bos_token="<|startoftext|>",
        eos_token="<|endoftext|>",
        pad_token="<|pad|>"
    )

    dataset_strs = []
    with open('./output/cfg-generated-%s.txt' % eval_run_name, 'r') as data:
        split_ds = data.read().split("<|splitter|>")
        dataset_strs = [text for text in split_ds]
    token_lengths = [len(fine_tokenizer.encode(ds_str)) for ds_str in dataset_strs]
    max_length = max(token_lengths)
    avg_length = int(sum(token_lengths) / len(token_lengths))
    print("Average/Max length to pad: %d/%d" % (avg_length, max_length))
    dataset = ProgramDataset(dataset_strs, fine_tokenizer, make_max_length(max_length, fine_tokenizer))

    with open('./output/gpt-eval-%s.txt' % eval_run_name, 'w') as file:
        for eval_ex in dataset:
            eval_input_ids = eval_ex[0]
            chopped_str = fine_tokenizer.decode(eval_input_ids).split("Program:")
            input_str = chopped_str[0] + "\nProgram:\n"
            input_tensor = fine_tokenizer.encode(input_str, return_tensors="pt").cuda()
            outputs = fine_model.generate(
                input_tensor, 
                max_length=1200,  
                # num_return_sequences=5,
                # no_repeat_ngram_size=2,
                # repetition_penalty=1.5,
                top_p=0.95,
                temperature=.55,
                do_sample=True,
                top_k=50,
                # early_stopping=False
            )
            total_output = fine_tokenizer.decode(outputs[0])
            total_output = total_output.replace("<|startoftext|>", "<|splitter|>")
            total_output = total_output.replace("<|endoftext|>", "")
            file.write(total_output)