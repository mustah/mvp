import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../../../reducers/rootReducer';
import {createBatchReference} from '../../../../../state/domain-models-paginated/batch-references/batchReferenceApiActions';
import {fetchDevices} from '../../../../../state/domain-models-paginated/devices/deviceApiActions';
import {
  getPageIsFetching,
  getSelectableDevices
} from '../../../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {getOrganisationId} from '../../../../auth/authSelectors';

import {BatchReferenceForm, DispatchToProps, StateToProps} from '../components/BatchReferenceForm';

const mapStateToProps = ({auth, paginatedDomainModels: {devices}}: RootState): StateToProps => ({
  devices: getSelectableDevices(devices),
  isFetchingDevices: getPageIsFetching(devices),
  organisationId: getOrganisationId(auth),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  saveBatchReference: createBatchReference,
  fetchDevices,
}, dispatch);

export const BatchReferenceFormContainer = connect(mapStateToProps, mapDispatchToProps)(BatchReferenceForm);
