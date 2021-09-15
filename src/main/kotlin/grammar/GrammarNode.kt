package grammar
class GrammarNode(productionRule: AttributedProductionRule,
                  var parent: GrammarNode,
                  var idx: Int = 0) : GenericGrammarNode(productionRule){

    override fun inheritedAttributes(): Set<NodeAttribute> {
        val siblingAttrs = parent.rhs.map { grammarNode: GrammarNode ->
            grammarNode.synthesizedAttributes()
        }
        return productionRule.makeInheritedAttributes(idx, parent.attributes(), siblingAttrs)
    }

}