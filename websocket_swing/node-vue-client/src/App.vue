<template>
  <div id="app">
  <div id="header">
  <span id="logo"></span>
  <span>Search Mock</span>
  </div>
  <Search content_textarea="Search Mock"/>
  </div>
</template>

<script>
import Search from './components/Search.vue'
export default {
  name: 'app',
  components: {
    Search
  }
}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
#header {
	color: #fff;
	background-color: #ff2b06;
	font-size: 40pt;
	margin: 0px;
	padding: 0;
	height: 80px;
}
#logo {
	position:absolute;
	left: 0;
	display:inline-block;
	background-size: 80px;
	background-repeat: no-repeat;
	width: 120px;
	height: 80px;
}
</style>