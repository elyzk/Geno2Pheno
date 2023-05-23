package edu.ncssm.elyzkatz.geno2pheno

import androidx.compose.runtime.Composable

class Nucleotides(_nucleotides : Array<Nucleotide>) {
    private val nucleotides = _nucleotides

    fun replaceNucleotide(num : Int, base : String): Boolean {
        if (num < 0 || num >= nucleotides.size) {
            return false
        }
        if (!nucleotides.get(num).setBase(base)) {
            return false
        }
        return true
    }

    fun getNucleotides() : Array<Nucleotide> {
        return nucleotides
    }
}