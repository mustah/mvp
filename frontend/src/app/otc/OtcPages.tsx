import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {UserAddContainer} from '../../usecases/administration/containers/UserAddContainer';
import {UserEditContainer} from '../../usecases/administration/containers/UserEditContainer';
import {BatchReferences} from '../../usecases/administration/otc/pages/BatchReferences';
import {BatchReferencesCreate} from '../../usecases/administration/otc/pages/BatchReferencesCreate';
import {Devices} from '../../usecases/administration/otc/pages/Devices';
import {DevicesAdd} from '../../usecases/administration/otc/pages/DevicesAdd';
import {Keys} from '../../usecases/administration/otc/pages/Keys';
import {OtcUsers} from '../../usecases/administration/otc/pages/Users';
import {routes} from '../routes';

export const OtcPages = () => (
  <Switch>
    <Route exact={true} path={routes.otc} component={OtcUsers}/>
    <Route exact={true} path={routes.otcUsersAdd} component={UserAddContainer}/>
    <Route exact={true} path={`${routes.otcUsersModify}/:userId`} component={UserEditContainer}/>

    <Route exact={true} path={routes.otcDevices} component={Devices}/>
    <Route exact={true} path={routes.otcDevicesAdd} component={DevicesAdd}/>

    <Route exact={true} path={routes.otcBatchReferences} component={BatchReferences}/>
    <Route exact={true} path={routes.otcBatchReferencesCreate} component={BatchReferencesCreate}/>
    <Route exact={true} path={`${routes.otcBatchReferencesModify}/:id`} component={BatchReferencesCreate}/>

    <Route exact={true} path={routes.otcKeys} component={Keys}/>

    <Redirect to={routes.otc}/>
  </Switch>
);
