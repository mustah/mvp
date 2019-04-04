import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {logout} from '../../auth/authActions';
import {getUser} from '../../auth/authSelectors';
import {DispatchToProps, Profile, StateToProps} from '../component/Profile';

const mapStateToProps = ({auth}: RootState): StateToProps => ({
  user: getUser(auth),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  logout,
}, dispatch);

export const ProfileContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(Profile);
