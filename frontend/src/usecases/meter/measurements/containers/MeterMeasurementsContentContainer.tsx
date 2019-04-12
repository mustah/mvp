import {connect} from 'react-redux';
import {RootState} from '../../../../reducers/rootReducer';
import {MeterDetails} from '../../../../state/domain-models/meter-details/meterDetailsModels';
import {ToolbarView} from '../../../../state/ui/toolbar/toolbarModels';
import {MeterMeasurementsContent} from '../components/MeterMeasurementsContent';

interface StateToProps {
  view: ToolbarView;
}

export interface OwnProps {
  meter: MeterDetails;
  useCollectionPeriod: boolean;
}

export type Props = StateToProps & OwnProps;

const mapStateToProps = ({ui: {toolbar: {meterMeasurement: {view}}}}: RootState): StateToProps => ({view});

export const MeterMeasurementsContentContainer =
  connect<StateToProps, null, OwnProps>(mapStateToProps)(MeterMeasurementsContent);
