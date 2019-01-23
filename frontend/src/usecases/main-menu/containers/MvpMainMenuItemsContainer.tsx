import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname, isMeterPage, isReportPage} from '../../../selectors/routerSelectors';
import {resetSelection} from '../../../state/user-selection/userSelectionActions';
import {OnClick, PathNamed} from '../../../types/Types';
import {MainMenuItems} from '../components/menu-items/MainMenuItems';

interface StateToProps extends PathNamed {
  isReportPage: boolean;
  isMeterPage: boolean;
}

interface DispatchToProps {
  resetSelection: OnClick;
}

export type MainMenuItemProps = StateToProps & DispatchToProps;

const mapStateToProps = ({routing}: RootState): StateToProps => ({
  pathName: getPathname(routing),
  isMeterPage: isMeterPage(routing),
  isReportPage: isReportPage(routing),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  resetSelection,
}, dispatch);

export const MvpMainMenuItemsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MainMenuItems);
