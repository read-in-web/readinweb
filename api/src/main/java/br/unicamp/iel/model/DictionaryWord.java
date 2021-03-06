package br.unicamp.iel.model;
// Generated Feb 6, 2015 10:24:22 AM by Hibernate Tools 3.2.2.GA



/**
 * DictionaryWord generated by hbm2java
 */
public class DictionaryWord  implements java.io.Serializable {


     private Long id;
     private Activity activity;
     private String word;
     private String meaning;

    public DictionaryWord() {
    }

	
    public DictionaryWord(Activity activity) {
        this.activity = activity;
    }
    public DictionaryWord(Activity activity, String word, String meaning) {
       this.activity = activity;
       this.word = word;
       this.meaning = meaning;
    }
   
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    public Activity getActivity() {
        return this.activity;
    }
    
    public void setActivity(Activity activity) {
        this.activity = activity;
    }
    public String getWord() {
        return this.word;
    }
    
    public void setWord(String word) {
        this.word = word;
    }
    public String getMeaning() {
        return this.meaning;
    }
    
    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }




}


