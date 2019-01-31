import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname, isMeterPage, isReportPage} from '../../../selectors/routerSelectors';
import {PathNamed} from '../../../types/Types';
import {MainMenuItems} from '../components/menu-items/MainMenuItems';

export interface StateToProps extends PathNamed {
  isReportPage: boolean;
  isMeterPage: boolean;
}

const mapStateToProps = ({routing}: RootState): StateToProps => ({
  pathName: getPathname(routing),
  isMeterPage: isMeterPage(routing),
  isReportPage: isReportPage(routing),
});

export const MvpMainMenuItemsContainer =
  connect<StateToProps>(mapStateToProps)(MainMenuItems);
