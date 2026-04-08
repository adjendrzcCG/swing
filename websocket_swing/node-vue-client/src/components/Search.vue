<template>
  <div id="step_form">
    <div id="person">
      <input v-model="formdata.last" placeholder="Name">
      <input v-model="formdata.knr" placeholder="Kundennummer" disabled>
      <input v-model="formdata.iban" placeholder="IBAN" disabled>
      <div class="form-nl"></div>
      <input v-model="formdata.first" placeholder="Vorname">
      <input v-model="formdata.betriebsbez" placeholder="Betriebsbez." disabled>
      <input v-model="formdata.bic" placeholder="BIC" disabled>
      <div class="form-nl"></div>
      <input v-model="formdata.dob" placeholder="Gebohren">
      <input v-model="formdata.rvnr" placeholder="RV-Nummer" disabled>
      <input v-model="formdata.nationality" placeholder="Nationalität">
      <div class="form-nl" style="height:16px;"></div>
      <input v-model="formdata.zip" placeholder="PLZ">
      <input v-model="formdata.postfach" placeholder="Postfach">
      <input v-model="formdata.bgnr" placeholder="BG-Nummer" disabled>
      <div class="form-nl"></div>
      <input v-model="formdata.ort" placeholder="Ort">
      <input v-model="formdata.vorsatzwort" placeholder="Vorsatzwort">
      <input v-model="formdata.leistung" placeholder="Leistung">
      <div class="form-nl"></div>
      <input v-model="formdata.street" placeholder="Strasse">
      <input v-model="formdata.title" placeholder="Titel">
      <input v-model="formdata.traegernr" placeholder="Träger-Nr. der gE." disabled>
      <div class="form-nl"></div>
      <input v-model="formdata.hausnr" placeholder="Hausnummer">
      <button v-on:click="searchPerson();">Suchen</button>
    </div>

    <div id="search_result">
      <table cellpadding=4>
        <thead>
          <tr>
            <th>Kdn.-nr</th>
            <th>Name</th>
            <th>Vorname</th>
            <th>Geburtsdatum</th>
            <th>PLZ</th>
            <th>Strasse</th>
            <th>Ort</th>
          </tr>
        </thead>
        <tbody>
        <tr v-for="(item, index) in search_result" :value="item" :key="index" 
        @click="selectResult(item)" :class="{'highlight': (item.knr == selected_result.knr)}">
          <td>{{item.knr}}</td>
          <td>{{item.name}}</td>
          <td>{{item.first}}</td>
          <td>{{item.dob}}</td>
          <td>{{item.zip}}</td>
          <td>{{item.street}} {{item.hausnr}}</td>
          <td>{{item.ort}}</td>
        </tr>
        </tbody>
      </table>
    </div>

    <div id="search_result_zahlungsempfänger">
      <table cellpadding=4>
        <thead>
          <tr>
            <th>IBAN</th>
            <th>BIC</th>
            <th>Gültig ab</th>
          </tr>
        </thead>
        <tbody>
        <tr v-for="(item, index) in selected_result.zahlungsempfaenger" :value="item" :key="index" 
        @click="zahlungsempfaengerSelected(item)" :class="{'highlight': (item.iban == zahlungsempfaenger_selected.iban)}">
          <td>{{item.iban}}</td>
          <td>{{item.bic}}</td>
          <td>{{item.valid_from}}</td>
        </tr>
        </tbody>
      </table>
    </div>

    <div id="send_to_allegro">
      <button v-on:click="sendMessage(selected_result, 'textfield');">Nach ALLEGRO &uuml;bernehmen</button>
    </div>

    <div id="textarea">
      <textarea rows="4" v-model="internal_content_textarea" placeholder="add multiple lines"></textarea>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Search',
  props: {
    result_selected: String,
    content_textarea: String
  },
  data: function() {
    return {
      internal_content_textarea: this.content_textarea,
      formdata: {},
      search_result: [],
      selected_result: {},
      zahlungsempfaenger_selected : "",
      search_space: [{first:'Hans', name:'Mayer', dob:'1981-01-08', zip:'95183', ort:'Trogen', street:'Isaaer Str.', hausnr:'23', knr:'79423984', zahlungsempfaenger:[
          {iban:'DE27100777770209299700', bic:'ERFBDE8E759', valid_from:'2020-01-04', valid_until:'', type:''},
          {iban:'DE11520513735120710131', bic:'DRESDEFE152', valid_from:'2019-03-12', valid_until:'', type:''}
        ]},
        {first:'Linda', name:'Reitmayr', dob:'1979-05-12', zip:'92148', ort:'Hof', street:'Erst-Reuther-Str.', hausnr:'51', knr:'67485246', zahlungsempfaenger:[
          {iban:'DE02120300000000202051', bic:'BYLADEM1001', valid_from:'2020-11-01', valid_until:'', type:''}
        ]},
        {first:'Karl', name:'May', dob:'1964-11-02', zip:'10124', ort:'Berlin', street:'Schäferstr.', hausnr:'12', knr:'13725246', zahlungsempfaenger:[
          {iban:'DE02500105170137075030', bic:'INGDDEFF', valid_from:'2020-02-01', valid_until:'', type:''},
          {iban:'DE02100500000054540402', bic:'BELADEBE', valid_from:'2018-05-07', valid_until:'', type:''},
          {iban:'DE02300209000106531065', bic:'CMCIDEDD', valid_from:'2019-10-09', valid_until:'', type:''}
        ]},
        {first:'Jens', name:'Mueller', dob:'1999-04-21', zip:'14489', ort:'Potsdam', street:'August-Bebel-Str.', hausnr:'79', knr:'41125291', zahlungsempfaenger:[
          {iban:'DE02200505501015871393', bic:'HASPDEHH', valid_from:'2020-08-15', valid_until:'', type:''},
          {iban:'DE02100100100006820101', bic:'PBNKDEFF', valid_from:'2019-02-21', valid_until:'', type:''}
        ]},
        {first:'Steffi', name:'Ruckmueller', dob:'1961-11-05', zip:'14432', ort:'Templin', street:'Charlottenstr.', hausnr:'16', knr:'31228193', zahlungsempfaenger:[
          {iban:'DE02370502990000684712', bic:'COKSDE33', valid_from:'2020-06-10', valid_until:'', type:''},
          {iban:'DE02701500000000594937', bic:'SSKMDEMM', valid_from:'2019-07-03', valid_until:'', type:''}
        ]}
      ]
    };
  },
  mounted:function(){
    this.connect();
  },
  methods: {
    connect() {
      this.socket = new WebSocket("ws://localhost:1337/");
      this.socket.onopen = () => {
        this.status = "connected";   
        //this.socket.onmessage = ({data}) => {};
      };
    },
    disconnect() {
      this.socket.close();
      this.status = "disconnected";
      this.logs = [];
    },
    searchPerson() {
      this.search_result = [];
      for(let i = 0; i < this.search_space.length; i++) {
        let element = this.search_space[i];
        if (this.formdata.last && element.name.toLowerCase().indexOf(this.formdata.last.toLowerCase()) >= 0
          || this.formdata.first && element.first.toLowerCase().indexOf(this.formdata.first.toLowerCase()) >= 0
          || this.formdata.zip && element.zip == this.formdata.zip
          || this.formdata.ort && element.ort.toLowerCase().indexOf(this.formdata.ort.toLowerCase()) >= 0
          || this.formdata.street && element.street.toLowerCase().indexOf(this.formdata.street.toLowerCase()) >= 0
          || this.formdata.hausnr && element.hausnr.toLowerCase().indexOf(this.formdata.hausnr.toLowerCase()) >= 0
        ) {
          this.search_result.push(element);
        }
      }
    },
    sendMessage(e, target) {
      let obj_to_send = (JSON.parse(JSON.stringify(e)));
      if (target == "textfield") {
        obj_to_send.zahlungsempfaenger = this.zahlungsempfaenger_selected;
      }
      this.socket.send(JSON.stringify({ target: target, content: obj_to_send }));
    },
    selectResult(item) {
      this.selected_result = item;
    },
    zahlungsempfaengerSelected(zahlungsempfaenger){
      this.zahlungsempfaenger_selected = zahlungsempfaenger;
    }
  },
  watch: {
    internal_content_textarea: function(val) {
      this.sendMessage(val, "textarea");
    }
  }
}
</script>

<style scoped>
*{
  box-sizing: border-box;
}

body, table{
  font-size: 12px;
}

div#step_form {
  position: relative;
  border: 1px solid red;
  background-color: #ffffff;
  margin: 40px;
  width: 540px;
  height: 680px;
}
div#step_form div {
  border: 1px solid lightgrey;
  width: 520px;
  margin: 10px;
  padding: 4px 4px 4px 0;
}
div#step_form div.form-nl {
  border: none;
  height: 4px;
  margin: 0;
  padding: 0;
}
div#step_form div input, button, textarea, select {
  margin-left: 4px;
}
div#search_result, div#search_result_zahlungsempfänger {
  text-align: left;
}
div#search_result table tr.highlight{
  background-color: #e8f0fe;
}
div#search_result table tr:hover{
  cursor: pointer;
}
div#search_result_zahlungsempfänger table tr.highlight{
  background-color: #c3e6cb;
}
div#search_result_zahlungsempfänger table tr:hover{
  cursor: pointer;
}
select, textarea {
  margin: 0 4px 0 4px;
  padding: 0;
  width: 510px;
}

div#person {
  position: relative;
  text-align: left;
}

div#person button {
  position: absolute;
  right: 4px;
}
div#person input {
  align: left;
  right: 0px;
}

div#send_to_allegro {
  text-align: right;
}
div#send_to_allegro button {
  display: inline-block;
}

div#textarea {
  position: absolute;
  bottom: 0px;
}
</style>