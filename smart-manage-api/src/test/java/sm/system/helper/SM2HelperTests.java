package sm.system.helper;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SM2HelperTests {
	private static final String PRIVATE_KEY =
			"00dc349799de9b5a3beef8d3d77aa7e3655c5542a10c395cc86670580b6250dfeb";
	private static final String PUBLIC_KEY =
			"0405102042204d15e802522ea09eb609358e4c72295e0ca44727eef75faac9afb3e"
					+ "eaa3e9df5af87e205e6ce5cb351e4fb30c37602f32aad969d2b222f05f8fa99";

	@Test
	void decryptsJsCiphertextWhoseCoordinateStartsLikeAPointPrefix() {
		SM2Helper helper = new SM2Helper();
		ReflectionTestUtils.setField(helper, "privateKey", PRIVATE_KEY);
		ReflectionTestUtils.setField(helper, "publicKey", PUBLIC_KEY);
		helper.init();
		String jsCiphertext = "02f2d4045ae1e3092a891676b9b2b2382aadb743f936ca1ef08fb0292499a2e4"
				+ "96113e3d73cf957567c549362dbb9a7e82ee85f6d3093ecd3f8b40976405212abe"
				+ "bafb83c5b1d3be9d1d753d3f7549d70b6513d0625c7de28edd58958f28c5f90f2c0077";

		assertEquals("ABCD", SM2Helper.decryptJsCiphertext(jsCiphertext));
	}
}
