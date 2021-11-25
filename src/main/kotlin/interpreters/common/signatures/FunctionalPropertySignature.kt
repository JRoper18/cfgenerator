package interpreters.common.signatures

import interpreters.common.signatures.PropertySignature

/**
 * Properties on functions that have a list of arguments and return 1 thing.
 */
abstract class FunctionalPropertySignature(inTypeNames : List<String>, outTypeName : String) : PropertySignature<List<Any>, Any>(inTypeNames, outTypeName){

}