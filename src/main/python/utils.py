import torch
from torch.utils.data import Dataset
import re
def find_nth(haystack, needle, n):
    start = haystack.find(needle)
    while start >= 0 and n > 1:
        start = haystack.find(needle, start+len(needle))
        n -= 1
    return start

def make_max_length(max_in, tokenizer):
    return min(max_in, tokenizer.model_max_length, 1200) # 1200 is highest # tokens can fit on 1 Shiva GPU. 

def wrap_str(in_str):
    return "<|startoftext|>" + in_str + "<|endoftext|>"

def remove_line_attrs(line, attr_regex):
    attrs_idx = line.find(" {")
    assert attrs_idx != -1
    attrs_str = line[attrs_idx:].replace("{", "").replace("}", "").strip()
    before_attrs = line[:attrs_idx]
    attrs = list(map(lambda attr_str: attr_str.strip(), attrs_str.split(",")))
    new_attrs = list(filter(lambda attr : re.match(attr_regex, attr.split("=")[0]), attrs))
    return before_attrs + " {" + ", ".join(new_attrs) + "}"

def remove_attrs(txt, attr_regex):
    new_lines = []
    in_program = False
    for line in txt.split("\n"):
        if in_program and line: # If the line's not empty and we're in the program
            new_line = remove_line_attrs(line, attr_regex)
        else:
            new_line = line
        new_lines.append(new_line)
        if line.strip() == "Program:":
            in_program = True
    return "\n".join(new_lines)

class ProgramDataset(Dataset):
    def __init__(self, txt_list, tokenizer, max_length, padding, attr_regex):
        self.input_ids = []
        self.attn_masks = []
        self.labels = []
        num_debug_examples = 10
        for idx, txt in enumerate(txt_list):
            if not txt:
                continue # No blank/empty strings.  
            built_txt = txt
            if attr_regex is not None:
                built_txt = remove_attrs(txt, attr_regex)
                if idx < num_debug_examples:
                    print("Before attr regex:")
                    print(txt)
                    print("After attr regex:")
                    print(built_txt)
            # Encode the descriptions using the GPT-Neo tokenizer
            encodings_dict = tokenizer(wrap_str(built_txt),
                                        truncation=False,
                                        max_length=max_length, 
                                        padding = padding)
            input_ids = encodings_dict["input_ids"]
            attn_mask = encodings_dict["attention_mask"]
            assert len(input_ids) == len(attn_mask)
            # Do the truncation ourselves so that truncation truncates from the head
            trunc_ids = input_ids[-max_length + 1:]
            trunc_attn = attn_mask[-max_length + 1:]
            assert len(trunc_ids) == len(trunc_attn)
            self.input_ids.append(torch.tensor(trunc_ids))
            mask = torch.tensor(trunc_attn)
            self.attn_masks.append(mask)
                
    def __len__(self):
        return len(self.input_ids)
    def __getitem__(self, idx):    
        return self.input_ids[idx], self.attn_masks[idx]
