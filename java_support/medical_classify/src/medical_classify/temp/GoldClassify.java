package medical_classify.temp;

import java.util.ArrayList;

public class GoldClassify {
	static void classify(String text) {
		ArrayList<String> codes = new ArrayList<>();

		// cluster 1
		// 789.00 : Abdominal pain, unspecified sit
		if (text.contains("abdominal pain") || text.contains("flank pain")) {
			codes.add("789.00");
		}
		// 789.09 : Abdominal pain, other specified site; multiple sites
		if (text.contains("quadrant pain") || (text.contains("hematuria") && text.contains("flank pain"))) {
			codes.add("789.09");
		}

		// cluser 2
		// V13.09 : Other personal history of disorders of urinary system
		if (false) { // todo: cannot do this

		}
		// 593.70 : Vesicoureteral reflux with reflux nephropathy, unspecified
		// or without reflux nephropathy
		if (text.contains("reflux") || text.contains("deflux")) {
			codes.add("593.70");
		}
		// V13.02 : Urinary (tract) infection
		if (text.contains("urinary tract infection") || text.contains("UTI")) {
			// codes.add("V13.02");
		}
		// 599.0 : Urinary tract infection, site not specified
		if (text.contains("urinary tract infection") || text.contains("UTI")) {
			codes.add("599.0");
		}
		// 596.8 : Other specified disorders of bladder
		if (false) { // something with bladder

		}

		// cluster 3
		// 753.3 : Other specified congenital anomalies of kidney
		if (text.contains("horseshoe kidney")) {
			codes.add("753.3");
		}
		// 592.0 : Calculus of kidney
		if ((text.indexOf("renal") != -1 && text.indexOf("calculi") != -1
				&& Math.abs(text.indexOf("renal") - text.indexOf("calculi")) <= 3)) {
			codes.add("592.0");
		}
		// 593.1 : Hypertrophy of kidney
		if (text.contains("hypertrophy")) {
			codes.add("593.1");
		}
		// V42.0 : Kidney replaced by transplant
		if (text.contains("renal transplant")) {
			codes.add("V42.0");
		}
		// 593.89 : Other specified disorders of kidney and ureter
		if (text.contains("kidney") && (!codes.contains("753.3") && !codes.contains("592.0") && !codes.contains("593.1")
				&& !codes.contains("V42.0"))) {
			codes.add("593.89");
		}
		// cluster
		// 462 : Acute pharyngitis
		if (text.contains("pharyngitis") || text.contains("sore throat")) {
			codes.add("462");
		}
		// 783.0 : Anorexia
		if (text.contains("loss of appetite")) {
			codes.add("783.0");
		}
		// 518.0 : Pulmonary collapse
		if ((text.indexOf("atelectasis") != -1 && text.indexOf("lobe") != -1
				&& Math.abs(text.indexOf("atelectasis") - text.indexOf("lobe")) <= 5)
				|| (text.indexOf("atelectasis") != -1 && text.indexOf("lung") != -1
						&& Math.abs(text.indexOf("atelectasis") - text.indexOf("lung")) <= 5)
				|| text.contains("lobe collapse")) {
			codes.add("518.0");
		}
		// 596.54 : Neurogenic bladder NOS
		if (text.contains("neurogenic bladder")) {
			codes.add("596.54");
		}
		// 786.50 : Unspecified chest pain
		if (text.contains("chest pain")) {
			codes.add("786.50");
		}
		// 786.2 : Cough
		if (text.contains("cough")) {
			codes.add("786.2");
		}
		// 277.00 : Cystic fibrosis without mention of meconium ileus
		if (text.contains("cystic fibrosis")) {
			codes.add("277.00");
		}
		// 780.6 : Fever
		if (text.contains("fever")) {
			codes.add("780.6 ");
		}
		// 758.6 : Gonadal dysgenesis
		if (text.contains("turner syndrome") || text.contains("gonadal dysgenesis")) {
			codes.add("758.6");
		}
		// 599.7 : Hematuria
		if (text.contains("hematuria")) {
			codes.add("599.7");
		}
		// 591 : Hydronephrosis
		if (text.contains("hydronephrosis")) {
			codes.add("591");
		}

		// 786.59 : Other chest pain
		if (text.contains("chest tightness")) {
			codes.add("786.59");
		}
		// 786.09 : Other dyspnea and respiratory abnormality
		if (text.contains("hypoventilation") || text.contains("breathlessness")) {
			codes.add("786.09");
		}
		// 511.9 : Unspecified pleural effusion
		if (text.contains("pleural effusion")) {
			codes.add("511.9");
		}
		// 791.0 : Proteinuria
		if (text.contains("proteinuria")) {
			codes.add("791.0");
		}
		// 788.30 : Urinary incontinence, unspecified
		if (text.contains("enuresis") || text.contains("urinary incontinence") || text.contains("night wetting")) {
			codes.add("788.30");
		}
		// 786.07 : Wheezing
		if (text.contains("wheeze")) {
			codes.add("786.07");
		}
		// 279.12 : Wiskott-Aldrich syndrome
		if (text.contains("Wiskott - Aldrich syndrome")) {
			codes.add("279.12");
		}
		// 079.99 : Unspecified viral infection in conditions classified
		// elsewhere and of unspecified site
		if (false) { // Viral airways disease
			codes.add("");
		}
		// 493.90 : Asthma, unspecified type, without mention of status
		// asthmaticus
		if (text.contains("asthma") || text.contains("reactive airways disease")) {
			codes.add("493.90");
		}

		// 741.90 : Spina bifida, without mention of hydrocephalus, unspecified
		// region
		if (text.contains("meningomyelocele") || text.contains("myelomeningocele") || text.contains("spina bifida")) {
			codes.add("741.90");
		}
		// 753.0 : Renal agenesis and dysgenesis
		if (text.contains("unilateral")) {
			codes.add("753.0");
		}
		// 795.5 : Nonspecific reaction to tuberculin skin test without active
		// tuberculosis
		if (text.contains("PPD")) {
			codes.add("795.5");
		}
		// 486 : Pneumonia, organism unspecified
		if (text.contains("pneumonia")) {
			codes.add("486");
		}
		// 753.21 : Congenital obstruction of ureteropelvic junction
		if (text.contains("ureteropelvic junction obstruction")) {
			codes.add("753.21");
		}
		// 786.05 : Shortness of breath
		if (text.contains("shortness of breath")) {
			codes.add("786.05");
		}
		// 759.89 : Other specified anomalies
		if (text.contains("Wiedemann syndrome") || text.contains("hemihypertrophy")) {
			codes.add("759.89");
		}
		// 785.6 : Enlargement of lymph nodes
		if (text.contains("lymphadenopathy")) {
			codes.add("785.6");
		}
		// 593.5 : Hydroureter
		if (text.contains("hydroureter")) {
			codes.add("593.5");
		}
		// 788.41 : Urinary frequency
		if (text.contains("hematuria") && text.contains("frequency")) {
			codes.add("788.41");
		}
		// 787.03 : Vomiting alone
		if (text.contains("renal vomiting")) {
			codes.add("787.03");
		}
		// V72.5 : Radiological examination, not elsewhere classified
		if (text.contains("none given")) {
			codes.add("V72.5");
		}
		// V67.09 : Follow-up examination following other surgery
		if (text.contains("status post")) {
			codes.add("V67.09");
		}
	}
}
