import IconButton from 'material-ui/IconButton';
import ContentClear from 'material-ui/svg-icons/content/clear';
import TextField from 'material-ui/TextField';
import * as React from 'react';
import {colors} from '../../../../app/colors';
import {underlineFocusStyle} from '../../../../app/themes';
import {ButtonLink} from '../../../../components/buttons/ButtonLink';
import {ThemeContext, withCssStyles} from '../../../../components/hoc/withThemeProvider';
import {RowBottom, RowLeft} from '../../../../components/layouts/row/Row';
import {idGenerator} from '../../../../helpers/idGenerator';
import {firstUpperTranslated, translate} from '../../../../services/translationService';
import {Clickable, IdNamed, uuid} from '../../../../types/Types';
import './InlineEditInput.scss';
import {DispatchToProps, StateToProps} from './SelectionMenu';

const textFieldStyle: React.CSSProperties = {
  margin: 0,
  fontSize: 24,
  fontWeight: 'bold',
  width: 320,
};

const inputStyle: React.CSSProperties = {
  color: colors.black,
};

const hintStyle: React.CSSProperties = {
  color: colors.borderColor,
};

const underlineStyle: React.CSSProperties = {
  borderWidth: 0,
};

const style: React.CSSProperties = {
  cursor: 'pointer',
  position: 'relative',
  padding: 0,
  left: -22,
  top: 0,
  width: 24
};

const buttonStyle: React.CSSProperties = {
  marginLeft: 16,
  marginBottom: 8,
};

const ResetIconButton = withCssStyles(({cssStyles: {primary}, onClick}: Clickable & ThemeContext) => {
  const iconStyle: React.CSSProperties = {
    width: 22,
    height: 22,
    color: primary.fg,
  };
  return (
    <IconButton
      onClick={onClick}
      style={style}
      iconStyle={iconStyle}
      touch={true}
      tooltip={translate('reset selection')}
      tooltipPosition="top-center"
    >
      <ContentClear hoverColor={colors.black} style={iconStyle}/>
    </IconButton>
  );
});

interface Props extends DispatchToProps, StateToProps, ThemeContext {
  isChanged: boolean;
}

interface State extends IdNamed {
  isChanged: boolean;
}

const isInitialSelection = (id: uuid) => id === -1;
const isSavedSelection = (id: uuid) => id !== -1;

export class InlineEditInput extends React.Component<Props, State> {

  constructor(props) {
    super(props);
    const {selection: {name, id}, isChanged} = props;
    this.state = {
      isChanged,
      name: isInitialSelection(id) ? '' : name,
      id,
    };
  }

  renderActionButtons = (): React.ReactNode => {
    const {id} = this.state;
    return (
      <RowLeft>
        {isSavedSelection(id) && <ButtonLink onClick={this.onSave} style={buttonStyle}>{translate('save')}</ButtonLink>}
        <ButtonLink onClick={this.onSaveAs} style={buttonStyle}>{translate('save as')}</ButtonLink>
      </RowLeft>
    );
  }

  renderSelectionResetButton = (): React.ReactNode => {
    const {selection: {id}, resetToSavedSelection} = this.props;
    const reset = () => resetToSavedSelection(id);
    return <ButtonLink onClick={reset} style={buttonStyle}>{translate('discard changes')}</ButtonLink>;
  }

  onChange = (event: any): void => this.setState({name: event.target.value, isChanged: true});

  onSave = (): void => {
    const {updateSelection, selection} = this.props;
    const {name} = this.state;
    this.setState({isChanged: false});
    updateSelection({...selection, name});
  }

  onSaveAs = (): void => {
    const {saveSelection, selection} = this.props;
    const {name} = this.state;
    const id = idGenerator.uuid();
    this.setState({id, isChanged: false});
    saveSelection({...selection, name, id});
  }

  render() {
    const {cssStyles: {primary}, isChanged: changed, resetSelection} = this.props;
    const {isChanged, name, id} = this.state;
    const shouldRenderActionButtons = name && (isChanged || changed || isInitialSelection(id));
    const shouldRenderResetButton = changed && isInitialSelection(id) || isSavedSelection(id) && !changed;
    const shouldRenderResetSelectionButton = isSavedSelection(id) && changed;

    return (
      <RowBottom className="InlineEditInput">
        <TextField
          style={textFieldStyle}
          floatingLabelFocusStyle={{color: primary.bg}}
          inputStyle={inputStyle}
          hintStyle={hintStyle}
          underlineFocusStyle={underlineFocusStyle}
          underlineStyle={underlineStyle}
          hintText={firstUpperTranslated('give the selection a name')}
          value={name}
          onChange={this.onChange}
          id={`selection-${id}`}
        />
        {shouldRenderResetButton && <ResetIconButton onClick={resetSelection}/>}
        {shouldRenderActionButtons && this.renderActionButtons()}
        {shouldRenderResetSelectionButton && this.renderSelectionResetButton()}
      </RowBottom>
    );
  }

}
