package com.github.sanctum.panther.util;

class SignatureCheck {

	static final String SIG = "r.O.0.A.B.X.Q.A.D.k.h.l.b.X.B.m.Z.X.N.0.N.D.I.w.N.z.E.w";

	static boolean isValid(String sig) {
		if (sig == null) return false;
		return HFEncoded.of(sig).deserialize(null, String.class).equals("Hempfest420710");
	}

}
