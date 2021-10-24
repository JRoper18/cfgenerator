package grammars.common

import grammar.APR
import grammar.AttributedProductionRule
import grammar.PR
import grammar.Symbol

class TypeAnnotationRule(lhs : Symbol,
                                val rhs : Symbol,
                                val subruleVarnameAttributeKey : String,
                                val attrKeySuffix : String,
                                ) : APR(PR(lhs, listOf(rhs))) {

}