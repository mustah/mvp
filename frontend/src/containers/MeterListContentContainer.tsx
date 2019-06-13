import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {DispatchToProps, MeterListContent, StateToProps} from '../components/meters/MeterListContent';
import {RootState} from '../reducers/rootReducer';
import {
  addMetersOnPageToReport,
  clearMetersErrorOnPage,
  syncMetersOnPage
} from '../state/domain-models-paginated/meter/meterApiActions';
import {getFirstPageError} from '../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {OwnProps} from '../usecases/meter/meterModels';

const mapStateToProps = ({
  paginatedDomainModels: {meters},
  summary: {payload: {numMeters}},
  ui: {pagination: {meters: {totalElements}}}
}: RootState): StateToProps =>
  ({
    error: getFirstPageError(meters),
    hasContent: numMeters > 0 || totalElements > 0,
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addMetersOnPageToReport,
  clearMetersErrorOnPage,
  syncMetersOnPage,
}, dispatch);

export const MeterListContentContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(MeterListContent);
