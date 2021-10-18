import torch
from torch.utils.data import Dataset
class ProgramDataset(Dataset):
    def __init__(self, txt_list, tokenizer, max_length):
        self.input_ids = []
        self.attn_masks = []
        self.labels = []
        for txt in txt_list:
            # Encode the descriptions using the GPT-Neo tokenizer
            encodings_dict = tokenizer("<|startoftext|>" 
                                        + txt +    
                                        "<|endoftext|>",
                                        truncation=True,
                                        max_length=max_length, 
                                        padding="max_length")
            input_ids = torch.tensor(encodings_dict["input_ids"])    
            self.input_ids.append(input_ids)
            mask = torch.tensor(encodings_dict["attention_mask"])
            self.attn_masks.append(mask)
    def __len__(self):
        return len(self.input_ids)
    def __getitem__(self, idx):    
        return self.input_ids[idx], self.attn_masks[idx]
