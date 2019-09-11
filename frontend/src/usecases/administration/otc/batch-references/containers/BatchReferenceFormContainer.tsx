import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../../../reducers/rootReducer';
import {createBatchReference} from '../../../../../state/domain-models-paginated/batch-references/batchReferenceApiActions';
import {getOrganisationId} from '../../../../auth/authSelectors';

import {BatchReferenceForm, DispatchToProps, StateToProps} from '../components/BatchReferenceForm';

const mapStateToProps = ({auth}: RootState): StateToProps => ({
  organisationId: getOrganisationId(auth),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  saveBatchReference: createBatchReference,
}, dispatch);

export const BatchReferenceFormContainer = connect(mapStateToProps, mapDispatchToProps)(BatchReferenceForm);
