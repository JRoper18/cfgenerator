import torch
from transformers import GPTNeoForCausalLM, GPT2Tokenizer


def generate_from_prompt(prompt, num_attempts, tokenizer, model):
    final_prompt = prompt
    if prompt == "":
        final_prompt = "<|startoftext|>"
    tokens = tokenizer(final_prompt, return_tensors="pt").input_ids.cuda()
    curr_len = (tokens.shape[1])
    new_len = min(1200, curr_len)
    cutpoint = 0
    if new_len < curr_len :
        cutpoint = curr_len - new_len
    input_tensor = tokens[:, cutpoint:]
    tensor_outs = model.generate(
        input_tensor, 
        max_length=2048,  
        num_return_sequences=num_attempts,
        # no_repeat_ngram_size=2,
        # repetition_penalty=1.5,
        top_p=0.95,
        temperature=.75,
        do_sample=True,
        top_k=50,
        # early_stopping=True
    )
    return [tokenizer.decode(output) for output in tensor_outs]

def get_model(model_dir_base, param_size, model_run_name):
    model_dir = "%s/gpt-results-%s-%s" % (model_dir_base, param_size, model_run_name)

    # Now, make the outputs for us to evaluate:
    fine_model = GPTNeoForCausalLM.from_pretrained(model_dir).cuda()
    fine_tokenizer = GPT2Tokenizer.from_pretrained(model_dir, 
        bos_token="<|startoftext|>",
        eos_token="<|endoftext|>",
        pad_token="<|pad|>"
    )
    return fine_model, fine_tokenizer

def generate_gpt(eval_generated_fname,
    eval_output_generated_fname,
    model_run_name,
    model_dir_base,
    param_size = "125M",
    num_attempts=25,
     
):
    fine_model, fine_tokenizer = get_model(model_dir_base, param_size, model_run_name)
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
    max_num_attempts = 10
    avg_length = int(sum(token_lengths) / len(token_lengths))
    print("Average/Max length to pad: %d/%d" % (avg_length, max_length))
    with open(eval_output_generated_fname, 'w') as file:
        for idx, eval_ex in enumerate(dataset_strs):
            attempt_count = 0
            total_output = "<|splitter|>\n"
            while attempt_count < num_attempts:
                current_num_attempts = min(num_attempts - attempt_count, max_num_attempts)
                attempt_count += current_num_attempts
                outputs = generate_from_prompt(eval_ex, current_num_attempts, fine_tokenizer, fine_model)
                for output in outputs:
                    total_output += "<|attempt|>\n" + output
            total_output = total_output.replace("<|startoftext|>", "")
            total_output = total_output.replace("<|endoftext|>", "")
            file.write(total_output)
            if idx % 10 == 0:
                print("GPT Generated %d/%d" % (idx, len(dataset_strs)))


def generate_gpt_free(
    eval_output_generated_fname,
    model_run_name,
    model_dir_base,
    prompt,
    fake_in = "1",
    fake_out = "1",
    num_gen = 100,
    param_size = "125M",   
):
    fine_model, fine_tokenizer = get_model(model_dir_base, param_size, model_run_name)
    max_num_attempts = 10
    with open(eval_output_generated_fname, 'w') as file:
        for i in range(0, num_gen, max_num_attempts):
            outputs = generate_from_prompt(prompt, max_num_attempts, fine_tokenizer, fine_model)
            for output in outputs:
                total_output = "<|splitter|>\n<|attempt|>\nInputs: \n{}\nOutput: \n{}\nProgram: \n{}\n".format(fake_in, fake_out, output)
                total_output = total_output.replace("<|startoftext|>", "")
                total_output = total_output.replace("<|endoftext|>", "")
                file.write(total_output)
            print("GPT Generated %d/%d" % (i, num_gen))
