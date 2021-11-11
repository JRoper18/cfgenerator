#!/usr/bin/env python
# coding: utf-8
from .utils import ProgramDataset, make_max_length
from torch.utils.data import random_split
from transformers import Trainer, TrainingArguments
import torch
from transformers import GPTNeoForCausalLM, GPT2Tokenizer
import transformers
import os


def train_gpt(run_name, generated_path, output_dir, attr_regex = None, use_pretrained = False):
    param_size = "125M"
    pretrained_name = "EleutherAI/gpt-neo-%s" % param_size
    output_dir = "%s/gpt-results-%s-%s" % (output_dir, param_size, run_name)

    print(transformers.__version__)
    model = GPTNeoForCausalLM.from_pretrained(pretrained_name).cuda()
    tokenizer = GPT2Tokenizer.from_pretrained(pretrained_name, 
        bos_token="<|startoftext|>",
        eos_token="<|endoftext|>",
        pad_token="<|pad|>")
    if not use_pretrained:
        print("Not using pretrained weights! Using random instead")
        model = GPTNeoForCausalLM(model.config).cuda()     
    # Resize the token embeddings because we've just added 3 new tokens 
    model.resize_token_embeddings(len(tokenizer))

    mb = 10 ** 6
    t = torch.cuda.get_device_properties(0).total_memory / mb
    r = torch.cuda.memory_reserved(0) / mb
    a = torch.cuda.memory_allocated(0) / mb
    f = r-a  # free inside reserved
    print("Free memory: %dMB with %d/%d MB allocated" % (f, a, t))

    dataset_strs = []
    with open(generated_path, 'r') as data:
        split_ds = data.read().split("<|splitter|>")
        dataset_strs = [text for text in split_ds]
    print("Getting largest token lengths from %d examples" % len(dataset_strs))
    token_lengths = [len(tokenizer.encode(ds_str)) for ds_str in dataset_strs]
    max_length = max(token_lengths)
    avg_length = int(sum(token_lengths) / len(token_lengths))
    print("Average/Max length to pad: %d/%d" % (avg_length, max_length))

    # Now, tokenize the datasets.
    print("Str dataset size: %d" % len(dataset_strs))
    # Sometimes our model has a max length
    ds_max_length = make_max_length(max_length, tokenizer)
    # if(ds_max_length != max_length):
    #     print("WARNING!!! WE MUST TRUNCATE SENTENCES TO %d!!!" % ds_max_length)
    #     print("WE MIGHT LOSE A LOT OF EXAMPLES!")
    dataset = ProgramDataset(dataset_strs, tokenizer, ds_max_length, padding="max_length", attr_regex = attr_regex)
    train_size = int(0.95 * len(dataset))
    train_ds, eval_ds = random_split(dataset, [train_size, len(dataset) - train_size], generator=torch.Generator().manual_seed(42))
    print("Training size: %d" % train_size)



    # with torch.no_grad():
    #     print(torch.cuda.is_available()) 
    #     torch.cuda.empty_cache()

    # os.environ['CUDA_LAUNCH_BLOCKING'] = "1" 
    training_args = TrainingArguments(
        output_dir=output_dir,         # output directory
        num_train_epochs=4,              # total # of training epochs
        per_device_train_batch_size=1,  # batch size per device during training
        per_device_eval_batch_size=1,   # batch size for evaluation
        warmup_steps=0,                # number of warmup steps for learning rate scheduler
        weight_decay=0.01,               # strength of weight decay
        logging_dir='./logs',            # directory for storing logs
        logging_steps=1,
        # no_cuda = True, # Damn these GPUs really are small
        fp16=True, # For better memory and training speeds. 
        # deepspeed = "./src/main/resources/ds_config_zero2.json" # SPEED
    )

    print("Beginning training...")
        
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
    is_checkpoint = os.path.isdir(output_dir + "/checkpoint-500")
    train_result = trainer.train(resume_from_checkpoint=is_checkpoint)
    print("done training!")
    trainer.save_model(output_dir)

    print("Done saving trained GPT to file!")

