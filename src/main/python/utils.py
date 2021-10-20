import torch
from torch.utils.data import Dataset

def find_nth(haystack, needle, n):
    start = haystack.find(needle)
    while start >= 0 and n > 1:
        start = haystack.find(needle, start+len(needle))
        n -= 1
    return start

class ProgramDataset(Dataset):
    def __init__(self, txt_list, tokenizer, max_length):
        self.input_ids = []
        self.attn_masks = []
        self.labels = []
        for txt in txt_list:
            # Encode the descriptions using the GPT-Neo tokenizer
            built_txt = txt
            program_too_long = False
            encodings_dict = tokenizer("<|startoftext|>" 
                                        + built_txt +    
                                        "<|endoftext|>",
                                        truncation=False,
                                        max_length=max_length, 
                                        padding="max_length")
            input_ids = encodings_dict["input_ids"]
            # Do the truncation ourselves so that truncation truncates from the head
            trunc_ids = input_ids[len(input_ids)-max_length + 1:]
            trunc_attn = encodings_dict["attention_mask"][len(input_ids)-max_length + 1:]
            self.input_ids.append(torch.tensor(trunc_ids))
            mask = torch.tensor(trunc_attn)
            self.attn_masks.append(mask)
                
    def __len__(self):
        return len(self.input_ids)
    def __getitem__(self, idx):    
        return self.input_ids[idx], self.attn_masks[idx]
