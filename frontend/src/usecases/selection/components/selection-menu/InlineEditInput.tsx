import TextField from 'material-ui/TextField';
import * as React from 'react';
import {floatingLabelFocusStyle, underlineFocusStyle} from '../../../../app/themes';
import {ButtonLink} from '../../../../components/buttons/ButtonLink';
import {Row, RowBottom} from '../../../../components/layouts/row/Row';
import {idGenerator} from '../../../../helpers/idGenerator';
import {firstUpperTranslated, translate} from '../../../../services/translationService';
import {OnSelectSelection, UserSelection} from '../../../../state/user-selection/userSelectionModels';
import {IdNamed, OnClick, OnClickWithId, uuid} from '../../../../types/Types';
import './InlineEditInput.scss';

interface Props {
  isChanged: boolean;
  selection: UserSelection;
  saveSelection: OnSelectSelection;
  updateSelection: OnSelectSelection;
  resetSelection: OnClick;
  selectSavedSelection: OnClickWithId;
}

interface State extends IdNamed {
  isChanged: boolean;
}

const textFieldStyle: React.CSSProperties = {
  marginLeft: 16,
  fontSize: 14,
  width: 180,
};

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
      <Row>
        {isSavedSelection(id) && <ButtonLink onClick={this.onSave}>{translate('save')}</ButtonLink>}
        <ButtonLink onClick={this.onSaveAs}>{translate('save as')}</ButtonLink>
      </Row>
    );
  }

  renderResetButton = (): React.ReactNode =>
    <ButtonLink onClick={this.props.resetSelection}>{translate('reset selection')}</ButtonLink>

  renderSelectionResetButton = (): React.ReactNode => {
    const {id} = this.props.selection;
    const reset = () => this.props.selectSavedSelection(id);
    return <ButtonLink onClick={reset}>{translate('discard changes')}</ButtonLink>;
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
    const {isChanged, name, id} = this.state;
    const shouldRenderActionButtons = name && (isChanged || this.props.isChanged || isInitialSelection(id));
    const shouldRenderResetButton = this.props.isChanged && isInitialSelection(id) ||
      isSavedSelection(id) && !this.props.isChanged;
    const shouldRenderResetSelectionButton = isSavedSelection(id) && this.props.isChanged;

    return (
      <RowBottom className="InlineEditInput">
        <TextField
          style={textFieldStyle}
          floatingLabelFocusStyle={floatingLabelFocusStyle}
          hintText={firstUpperTranslated('give the selection a name')}
          underlineFocusStyle={underlineFocusStyle}
          value={name}
          onChange={this.onChange}
          id={`selection-${id}`}
        />
        {shouldRenderActionButtons && this.renderActionButtons()}
        {shouldRenderResetButton && this.renderResetButton()}
        {shouldRenderResetSelectionButton && this.renderSelectionResetButton()}
      </RowBottom>
    );
  }

}
