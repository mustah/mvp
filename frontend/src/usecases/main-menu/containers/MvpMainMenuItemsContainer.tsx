import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname, isMeterPage, isReportPage} from '../../../selectors/routerSelectors';
import {PathNamed} from '../../../types/Types';
import {getLegendItems} from '../../../state/report/reportSelectors';
import {MainMenuItems} from '../components/menu-items/MainMenuItems';

export interface ReportPageProps {
  isReportPage: boolean;
  numSelectedItems: number;
}

export interface StateToProps extends PathNamed, ReportPageProps {
  isMeterPage: boolean;
}

const mapStateToProps = ({router, report: {savedReports}}: RootState): StateToProps => ({
  isMeterPage: isMeterPage(router),
  isReportPage: isReportPage(router),
  numSelectedItems: getLegendItems(savedReports).length,
  pathName: getPathname(router),
});

export const MvpMainMenuItemsContainer = connect<StateToProps>(mapStateToProps)(MainMenuItems);
