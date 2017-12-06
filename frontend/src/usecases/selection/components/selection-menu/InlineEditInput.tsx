import TextField from 'material-ui/TextField';
import * as React from 'react';
import {floatingLabelFocusStyle, underlineFocusStyle} from '../../../../app/themes';
import {ButtonLink} from '../../../../components/buttons/ButtonLink';
import {Row, RowBottom} from '../../../../components/layouts/row/Row';
import {idGenerator} from '../../../../services/idGenerator';
import {translate} from '../../../../services/translationService';
import {OnSelectSelection, SelectionState} from '../../../../state/search/selection/selectionModels';
import {IdNamed, OnClick, uuid} from '../../../../types/Types';
import './InlineEditInput.scss';

interface Props {
  isChanged: boolean;
  selection: SelectionState;
  saveSelection: OnSelectSelection;
  updateSelection: OnSelectSelection;
  resetSelection: OnClick;
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
      name: isInitialSelection(id) ? '' : name, // TODO[!must!] translate texts outside of React components
      id,
    };
  }

  render() {
    const {isChanged, name, id} = this.state;
    const shouldRenderActionButtons = isChanged || this.props.isChanged || isInitialSelection(id);
    const shouldRenderResetButton = !shouldRenderActionButtons && isSavedSelection(id);

    return (
      <RowBottom className="InlineEditInput">
        <TextField
          style={textFieldStyle}
          floatingLabelFocusStyle={floatingLabelFocusStyle}
          hintText="Namnge ditt urval"
          underlineFocusStyle={underlineFocusStyle}
          value={name}
          onChange={this.onChange}
          id={`selection-${id}`}
        />
        {shouldRenderActionButtons && this.renderActionButtons()}
        {shouldRenderResetButton && this.renderResetButton()}
      </RowBottom>
    );
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

  renderResetButton = (): React.ReactNode => {
    return <ButtonLink onClick={this.props.resetSelection}>{translate('reset')}</ButtonLink>;
  }

  onChange = ({target: {value}}: React.ChangeEvent<any>): void => {
    this.setState({name: value, isChanged: true});
  }

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

}
