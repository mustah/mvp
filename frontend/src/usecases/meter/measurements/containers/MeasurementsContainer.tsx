import {connect} from 'react-redux';
import {RootState} from '../../../../reducers/rootReducer';
import {MeterDetails} from '../../../../state/domain-models/meter-details/meterDetailsModels';
import {ToolbarView} from '../../../../state/ui/toolbar/toolbarModels';
import {MeasurementContent} from '../components/MeasurementContent';

interface StateToProps {
  view: ToolbarView;
}

export interface OwnProps {
  meter: MeterDetails;
}

export type Props = StateToProps & OwnProps;

const mapStateToProps = ({ui: {toolbar: {collection: {view}}}}: RootState): StateToProps => ({view});

export const MeasurementsContainer = connect<StateToProps, null, OwnProps>(mapStateToProps)(MeasurementContent);
