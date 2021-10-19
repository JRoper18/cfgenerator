from collections.abc import Iterable, Iterator
def canBeList(x):
    return isinstance(x, Iterable)

def expand_iters(e):
    if(canBeList(e)): # Expand it out.
        return [expand_iters(ele) for ele in e]
    return e
def mapt(f, x):
    if(len(x) == 0):
        return f(x)
    # TODO: this
    return x
def cons(a, b):
    blist = canBeList(b)
    alist = canBeList(a)
    al = expand_iters(a) if alist else [a]
    bl = expand_iters(b) if blist else [b]
    return al + bl

foldl = lambda func, acc, xs: functools.reduce(func, xs, acc)
foldr = lambda func, acc, xs: functools.reduce(lambda x, y: func(y, x), xs[::-1], acc)
def recl(f, e, x):
    if(len(x) == 0):
        return e
    return f(x[0], x[1:])

