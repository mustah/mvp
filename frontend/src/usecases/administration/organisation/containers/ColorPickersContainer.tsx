import {connect} from 'react-redux';
import {compose} from 'recompose';
import {bindActionCreators} from 'redux';
import {withSuperAdminOnly} from '../../../../components/hoc/withRoles';
import {withCssStyles} from '../../../../components/hoc/withThemeProvider';
import {RootState} from '../../../../reducers/rootReducer';
import {changePrimaryColor, changeSecondaryColor, resetColors} from '../../../theme/themeActions';
import {ColorPickers, DispatchToProps, Props, StateToProps} from '../components/ColorPickers';

type ContainerProps = StateToProps & DispatchToProps;

const EnhancedColorPickers = compose<Props, ContainerProps>(withCssStyles, withSuperAdminOnly)(ColorPickers);

const mapStateToProps = ({theme: {color}}: RootState): StateToProps => ({color});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changePrimaryColor,
  changeSecondaryColor,
  resetColors,
}, dispatch);

export const ColorPickersContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(EnhancedColorPickers);
