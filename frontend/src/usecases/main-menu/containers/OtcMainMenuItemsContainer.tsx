import ActionSupervisorAccount from 'material-ui/svg-icons/action/supervisor-account';
import CommunicationVpnKey from 'material-ui/svg-icons/communication/vpn-key';
import DeviceDevices from 'material-ui/svg-icons/device/devices';
import DeviceDvr from 'material-ui/svg-icons/device/dvr';
import * as React from 'react';
import {connect} from 'react-redux';
import {routes} from '../../../app/routes';
import {Column} from '../../../components/layouts/column/Column';
import {Link} from '../../../components/links/Link';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname} from '../../../selectors/routerSelectors';
import {translate} from '../../../services/translationService';
import {mainMenuIconProps, MainMenuItem} from '../components/menu-items/MainMenuItem';

interface StateToProps {
  pathname: string;
}

const UsersLinkMenuItems = ({pathname}: StateToProps) => (
  <Link to={routes.otc}>
    <MainMenuItem
      name={translate('users')}
      isSelected={routes.otc === pathname}
      icon={<ActionSupervisorAccount {...mainMenuIconProps}/>}
    />
  </Link>
);

const DevicesLinkMenuItem = ({pathname}: StateToProps) => (
  <Link to={routes.otcDevices}>
    <MainMenuItem
      name={translate('my devices')}
      isSelected={routes.otcDevices === pathname}
      icon={<DeviceDevices {...mainMenuIconProps}/>}
    />
  </Link>
);

const BatchReferencesLinkMenuItem = ({pathname}: StateToProps) => (
  <Link to={routes.otcBatchReferences}>
    <MainMenuItem
      name={translate('batch references')}
      isSelected={routes.otcBatchReferences === pathname}
      icon={<DeviceDvr {...mainMenuIconProps}/>}
    />
  </Link>
);

const KeysLinkMenuItem = ({pathname}: StateToProps) => (
  <Link to={routes.otcKeys}>
    <MainMenuItem
      name={translate('my keys')}
      isSelected={routes.otcKeys === pathname}
      icon={<CommunicationVpnKey {...mainMenuIconProps}/>}
    />
  </Link>
);

const MainMenuItems = (props: StateToProps) => (
  <Column>
    <UsersLinkMenuItems {...props} />
    <DevicesLinkMenuItem {...props}/>
    <BatchReferencesLinkMenuItem {...props}/>
    <KeysLinkMenuItem {...props}/>
  </Column>
);

const mapStateToProps = ({router}: RootState): StateToProps => ({
  pathname: getPathname(router),
});

export const OtcMainMenuItemsContainer = connect(mapStateToProps)(MainMenuItems);
