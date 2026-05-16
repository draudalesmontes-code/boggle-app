import { HashRouter, Route, Routes } from 'react-router'
import './App.css'
import LoginPage from './pages/LoginPage'
import HomePage from './pages/HomePage'
import SignupPage from './pages/SignupPage'
import GamePage from './pages/GamePage'
import DictionaryPage from "./pages/DictionaryPage";
import StatsPage from "./pages/StatsPage";
import AccountPage from "./pages/AccountPage";
import HowToPlayPage from "./pages/HowToPlayPage";

function App() {
  return (
    <HashRouter>
      <Routes>
        <Route path = "/" element={<LoginPage/>}/>
        <Route path = "/login" element = {<LoginPage/>}/>
        <Route path = "/signup" element = {<SignupPage/>}/>
        <Route path = "/home" element = {<HomePage/>}/>
        <Route path = "/game" element = {<GamePage/>}/>
        <Route path = "/dictionary" element = {<DictionaryPage/>}/>
        <Route path = "/stats"      element = {<StatsPage/>}/>
        <Route path = "/account"    element = {<AccountPage/>}/>
        <Route path = "/how-to-play" element = {<HowToPlayPage/>}/>
      </Routes>
    </HashRouter>
  )
}

export default App
