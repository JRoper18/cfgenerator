import torch
from transformers import GPTNeoForCausalLM, GPT2Tokenizer
from .utils import ProgramDataset, make_max_length


def generate_gpt(eval_generated_fname,
    eval_output_generated_fname,
    model_run_name,
    model_dir_base,
    param_size = "125M",
     
):
    model_dir = "%s/gpt-results-%s-%s" % (model_dir_base, param_size, model_run_name)

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
                # If it's blank, skippit. 
                continue
            chopped_str = text.split("Program:")
            dataset_strs.append("%s\nProgram:" % chopped_str[0]) # Before the program, aka only examples. 
    token_lengths = [len(fine_tokenizer.encode(ds_str)) for ds_str in dataset_strs]
    max_length = max(token_lengths)
    avg_length = int(sum(token_lengths) / len(token_lengths))
    print("Average/Max length to pad: %d/%d" % (avg_length, max_length))
    with open(eval_output_generated_fname, 'w') as file:
        for idx, eval_ex in enumerate(dataset_strs):
            tokens = fine_tokenizer(eval_ex, return_tensors="pt").input_ids.cuda()
            curr_len = (tokens.shape[1])
            new_len = min(1200, curr_len)
            cutpoint = 0
            if new_len < curr_len :
                cutpoint = curr_len - new_len
            input_tensor = tokens[:, cutpoint:]
            # input_tensor = tokens
            outputs = fine_model.generate(
                input_tensor, 
                max_length=2048,  
                # num_return_sequences=3,
                # no_repeat_ngram_size=2,
                # repetition_penalty=1.5,
                top_p=0.95,
                temperature=.55,
                do_sample=True,
                top_k=50,
                # early_stopping=True
            )
            total_output = "<|splitter|>\n%s" % fine_tokenizer.decode(outputs[0])
            total_output = total_output.replace("<|startoftext|>", "")
            total_output = total_output.replace("<|endoftext|>", "")
            file.write(total_output)
            if idx % 10 == 0:
                print("GPT Generated %d/%d" % (idx, len(dataset_strs)))
