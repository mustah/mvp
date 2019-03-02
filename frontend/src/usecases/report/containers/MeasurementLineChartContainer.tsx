import {connect} from 'react-redux';
import {DateRange, Period} from '../../../components/dates/dateModels';
import {DispatchToProps} from '../../../components/tabs/components/MainContentTabs';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {hasMeasurementValues} from '../../../state/ui/graph/measurement/measurementSelectors';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {getSelectedPeriod} from '../../../state/user-selection/userSelectionSelectors';
import {uuid} from '../../../types/Types';
import {GraphComponent} from '../components/GraphComponent';
import {GraphContents} from '../reportModels';
import {hasLegendItems} from '../reportSelectors';

export interface OwnProps {
  outerHiddenKeys: uuid[];
  graphContents: GraphContents;
}

export interface StateToProps {
  customDateRange: Maybe<DateRange>;
  isSideMenuOpen: boolean;
  period: Period;
  hasMeters: boolean;
  hasContent: boolean;
}

const mapStateToProps = ({
  report: {savedReports},
  measurement,
  userSelection: {userSelection},
  ui,
}: RootState): StateToProps =>
  ({
    ...getSelectedPeriod(userSelection),
    isSideMenuOpen: isSideMenuOpen(ui),
    hasMeters: hasLegendItems(savedReports),
    hasContent: hasMeasurementValues(measurement.measurementResponse)
  });

export const MeasurementLineChartContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps)(GraphComponent);
