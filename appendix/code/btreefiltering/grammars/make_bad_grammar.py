def subsets(aList):
    if aList == []:  # base case
        yield []
    else:
        first = aList[0]
        rest = aList[1:]
        for ss in subsets(rest):  # include first or don't in each
            yield ss  # subset of rest
            yield [first] + ss


def make_grammar(tSize, tPrimeSize, grammar_name):
    t = []
    for i in range(0, tSize):
        t.append("a" + str(i))

    tPrime = []
    for i in range(0, tPrimeSize):
        tPrime.append("b" + str(i))

    with open(grammar_name, 'w+') as f:
        i = 0
        for ss in subsets(t):
            i += 1
            if len(ss) == 0:
                continue
            if len(ss) == tSize:
                ss.extend(tPrime)

            f.write('S->' + ' '.join(ss) + '\n')
            if len(ss) > 1:
                f.write('S->' + ' '.join(reversed(ss)) + '\n')


if __name__ == '__main__':
    for tSize in [11, 12]:
        for tPrimeSize in range(20000, 100001, 20000):
            grammar_name = 'bad_grammars/bad_grammar_{}_{}.txt'.format(tSize, tPrimeSize)
            make_grammar(tSize, tPrimeSize, grammar_name)
            print('done {} {}'.format(tSize, tPrimeSize))


