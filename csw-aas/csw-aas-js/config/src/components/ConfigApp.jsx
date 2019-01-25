import React from 'react'
import {BrowserRouter} from 'react-router-dom'
import {ClientRole, TMTAuthContextProvider} from 'csw-aas-js'
import NavComponent from './NavComponent'
import {AppConfig} from '../config/AppConfig'
import WriteConfig from './CreateConfig'
import ConfigError from './ConfigError'
import ListConfig from './ListConfig'
import GetConfig from './GetConfig'

const ConfigApp = () => {
  return (
    <div className='row card col s12 m7'>
      <TMTAuthContextProvider config={AppConfig}>
        <BrowserRouter>
          <div>
            <NavComponent />
          </div>
        </BrowserRouter>
        <ListConfig />
        <GetConfig />
        <ClientRole
          clientRole='admin'
          client='csw-config-server'
          error={<ConfigError />}>
          <WriteConfig />
        </ClientRole>
      </TMTAuthContextProvider>
    </div>
  )
}

export default ConfigApp