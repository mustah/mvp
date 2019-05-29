import {connect} from 'react-redux';
import {compose} from 'recompose';
import {bindActionCreators} from 'redux';
import {withSuperAdminOnly} from '../../../../components/hoc/withRoles';
import {withCssStyles} from '../../../../components/hoc/withThemeProvider';
import {RootState} from '../../../../reducers/rootReducer';
import {changePrimaryColor, changeSecondaryColor, fetchTheme, resetColors} from '../../../theme/themeActions';
import {ColorPickers, DispatchToProps, OwnProps, Props, StateToProps} from '../components/ColorPickers';

type ContainerProps = StateToProps & DispatchToProps;

const EnhancedColorPickers = compose<Props, ContainerProps>(withCssStyles, withSuperAdminOnly)(ColorPickers);

const mapStateToProps = ({theme: {color, isFetching, isSuccessfullyFetched}}: RootState): StateToProps => ({
  color,
  isFetching,
  isSuccessfullyFetched,
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changePrimaryColor,
  changeSecondaryColor,
  fetchTheme,
  resetColors,
}, dispatch);

export const ColorPickersContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(EnhancedColorPickers);
