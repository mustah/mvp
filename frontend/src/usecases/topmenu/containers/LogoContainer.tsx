import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {getOrganisationSlug} from '../../auth/authSelectors';
import {Logo, StateToProps} from '../component/Logo';

const mapStateToProps = ({auth}: RootState): StateToProps => ({
  organisationSlug: getOrganisationSlug(auth),
});

export const LogoContainer = connect<StateToProps>(mapStateToProps)(Logo);
