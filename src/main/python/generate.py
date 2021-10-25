import torch
from transformers import GPTNeoForCausalLM, GPT2Tokenizer
from .utils import make_max_length, wrap_str


def generate_gpt(eval_generated_fname,
    eval_output_generated_fname,
    model_run_name,
    param_size = "125M",
     
):
    model_dir = "./output/gpt-results-%s-%s" % (param_size, model_run_name)

    # Now, make the outputs for us to evaluate:
    fine_model = GPTNeoForCausalLM.from_pretrained(model_dir).cuda()
    fine_tokenizer = GPT2Tokenizer.from_pretrained(model_dir, 
        bos_token="<|startoftext|>",
        eos_token="<|endoftext|>",
        pad_token="<|pad|>"
    )

    dataset_strs = []
    with open(eval_generated_fname, 'r') as data:
        split_ds = data.read().split("<|splitter|>")
        for text in split_ds:
            if not text:
                # If it's blank
                continue
            chopped_str = text.split("Program:")
            dataset_strs.append("%s\nProgram:" % chopped_str[0]) # Before the program, aka only examples. 
    max_length = make_max_length(2048, fine_tokenizer)
    with open(eval_output_generated_fname, 'w') as file:
        for idx, eval_ex in enumerate(dataset_strs):
            wrapped = wrap_str(eval_ex)
            input_tensor = fine_tokenizer.encode(wrapped, return_tensors="pt")[:, -max_length:].cuda()
            outputs = fine_model.generate(
                input_tensor, 
                max_length=2048,  
                # num_return_sequences=3,
                # no_repeat_ngram_size=2,
                # repetition_penalty=1.5,
                top_p=0.90,
                temperature=.55,
                do_sample=True,
                top_k=50,
                # early_stopping=True
            )
            total_output = "<|splitter|>\n%s" % fine_tokenizer.decode(outputs[0])
            total_output = total_output.replace("<|startoftext|>", "")
            total_output = total_output.replace("<|endoftext|>", "")
            pad_idx = total_output.find("<|pad|>")
            if(pad_idx != -1):
                print(total_output)
                print("and input:")
                print(dataset_strs[idx])
                print("at index %d" % idx)
                break
            file.write(total_output)
            if idx % 10 == 0:
                print("GPT Generated %d/%d" % (idx, len(dataset_strs)))
