# CFGenerator

The CFGenerator repo is a set of tools for the synthesis of datasets for neural methods to train on, and a few tols for training on set datasets. The repo is largely split into two parts: 
* A Kotlin library and tool for specifying attribute grammars, and then generating large numbers of programs that satisfy those grammars. This library also includes tools and utilities for evaluating the quality of generated programs. 
* A series of small Python scripts for training a GPT-Neo model on a synthesized dataset. 

These tools can be tested and evaluated all at once using a script located in the rool directory "pipeline.py", which provides a simple interface for running all of these things without having to deal with language-specific details. For example, to run the entire pipeline on L2 programs, use the following command:
```
python3 pipeline.py --allname=lambda2 --language=lambda2 --do_all
```
"Freely" generating examples given a prompt, either for debugging or evaluation purposes, can be done using the free_gen and prompt options: 
```
python3 pipeline.py --allname=haskell --language=haskell --do_free_gen --free_prompt prompt.txt
```

Further command line options can be found by investigating the script itself, or by checking the help info: 

```
python3 pipeline.py --help
```


## Implementing this process for your own language
If you want to implement your own language, you can extend the Language<I, O> interface, where I are the types of inputs and O are the types of outputs. For most languages, these will be generic "Any"s. The Language interface requires you make methods to generate programs and examples, run programs on an example, turn examples and programs into string representations, and provide a grammar. Not all of these methods are needed for all parts of evaluation: For example, rather than specify the entire Haskell grammar, the Haskell language simply leaves a placeholder implementation, and only implements the sections of the interface that are neccesary for evaluation. That way, the pipeline script can be used with Haskell during evaluation. 

For generating programs efficiently, you must specify a grammar. Examples of grammars can be found in the /grammars package. Specifying a grammar requires you specify a set of Attributed Production Rules, which are production rules associated with attribute-generating functions. Production Rules require you specify a single LHS symbol and a list of RHS symbols, which can be terminal or non-terminal. 

Specifying Attributed Production Rules requires you specify a production rule, procedures for generating synthesized and inhereted attributes, and a function "canMakeProgramWithAttributes" which turns a set of attributes and turns them into a set of equivalent wanted attribute on children. Common attributed production rules and attribute/constraint functions can be found in the grammars/common/rules package. 

The last feature you need to specify a grammar is a set of constraints. These are a map from production rules to constraint generating functions on those rules. While attributed production rules have procedures for transforming attributes into child attributes and vice-versa, the constraint generators take in a set of attributes and generate more formal constraints on the rule itself. While this functionality can be mimiced with just attributed produciton rules, this interface allows for a nicer seperation between attribute functions and constraint functions.

Once your language is complete, add the language to the subscripts/Utils.kt language list, and you're ready to make a training set. 