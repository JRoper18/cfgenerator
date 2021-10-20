#!/usr/bin/env python
# coding: utf-8



def train_gpt(run_name, generated_path):
    param_size = "125M"
    pretrained_name = "EleutherAI/gpt-neo-%s" % param_size
    output_dir = "./results-%s-%s" % (param_size, run_name)





    from transformers import GPTNeoForCausalLM, GPT2Tokenizer
    import transformers
    print(transformers.__version__)
    model = GPTNeoForCausalLM.from_pretrained(pretrained_name).cuda()
    tokenizer = GPT2Tokenizer.from_pretrained(pretrained_name, 
        bos_token="<|startoftext|>",
        eos_token="<|endoftext|>",
        pad_token="<|pad|>")
    # Resize the token embeddings because we've just added 3 new tokens 
    model.resize_token_embeddings(len(tokenizer))





    mb = 10 ** 6
    import torch
    t = torch.cuda.get_device_properties(0).total_memory / mb
    r = torch.cuda.memory_reserved(0) / mb
    a = torch.cuda.memory_allocated(0) / mb
    f = r-a  # free inside reserved
    print("Free memory: %dMB with %d/%d MB allocated" % (f, a, t))





    dataset_strs = []
    with open(generated_path, 'r') as data:
        split_ds = data.read().split("<|splitter|>")
        dataset_strs = [text for text in split_ds]
    token_lengths = [len(tokenizer.encode(ds_str)) for ds_str in dataset_strs]
    max_length = max(token_lengths)
    avg_length = sum(token_lengths) / len(token_lengths)
    print("Average/Max length to pad: %d/%d" % (avg_length, max_length))


    # Now, tokenize the datasets.

    from torch.utils.data import random_split
    print("Str dataset size: %d" % len(dataset_strs))
    dataset = ProgramDataset(dataset_strs, tokenizer, max_length)
    train_size = int(0.9 * len(dataset))
    train_ds, eval_ds = random_split(dataset, [train_size, len(dataset) - train_size], generator=torch.Generator().manual_seed(42))
    print("Training size: %d" % train_size)



    from transformers import Trainer, TrainingArguments
    import torch
    # with torch.no_grad():
    #     print(torch.cuda.is_available()) 
    #     torch.cuda.empty_cache()

    # import os
    # os.environ['CUDA_LAUNCH_BLOCKING'] = "1" 
    training_args = TrainingArguments(
        output_dir=output_dir,         # output directory
        num_train_epochs=4,              # total # of training epochs
        per_device_train_batch_size=2,  # batch size per device during training
        per_device_eval_batch_size=2,   # batch size for evaluation
        warmup_steps=0,                # number of warmup steps for learning rate scheduler
        weight_decay=0.01,               # strength of weight decay
        logging_dir='./logs',            # directory for storing logs
        logging_steps=1,
        # no_cuda = True, # Damn these GPUs really are small
        fp16=True # FOr better memory and training speeds. 
    )
        
    trainer = Trainer(
        model=model,
        args=training_args,
        train_dataset=train_ds,
        eval_dataset=eval_ds,
        tokenizer=tokenizer,
        # Data collator will default to DataCollatorWithPadding, so we change it
        data_collator=lambda data: 
                {"input_ids": torch.stack([f[0] for f in data]),       
                "attention_mask": torch.stack([f[1] for f in data]),
                "labels": torch.stack([f[0] for f in data])}
    )
    # train_result = trainer.train()
    train_result = trainer.train(resume_from_checkpoint=True)
    print("done training!")
    trainer.save_model(output_dir)

    print("Done saving trained GPT to file!")

