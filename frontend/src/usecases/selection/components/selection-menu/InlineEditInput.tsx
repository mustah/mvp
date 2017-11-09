import 'InlineEditInput.scss';
import TextField from 'material-ui/TextField';
import * as React from 'react';
import {idGenerator} from '../../../../services/idGenerator';
import {translate} from '../../../../services/translationService';
import {OnSelectSelection, SelectionState} from '../../../../state/search/selection/selectionModels';
import {IdNamed} from '../../../../types/Types';
import {floatingLabelFocusStyle, underlineFocusStyle} from '../../../app/themes';
import {LinkButton} from '../../../common/components/buttons/LinkButton';
import {Row, RowBottom} from '../../../common/components/layouts/row/Row';

interface Props {
  selection: SelectionState;
  saveSelection: OnSelectSelection;
  updateSelection: OnSelectSelection;
}

interface State extends IdNamed {
  isChanged: boolean;
}

const textFieldStyle: React.CSSProperties = {
  marginLeft: 16,
  fontSize: 14,
  width: 180,
};

export class InlineEditInput extends React.Component<Props, State> {

  constructor(props) {
    super(props);
    const {selection: {name, id}} = props;
    this.state = {
      isChanged: false,
      name,
      id,
    };
  }

  render() {
    const {isChanged, name, id} = this.state;

    return (
      <RowBottom className="InlineEditInput">
        <TextField
          style={textFieldStyle}
          floatingLabelFocusStyle={floatingLabelFocusStyle}
          underlineFocusStyle={underlineFocusStyle}
          value={name}
          onChange={this.onChange}
          id={`selection-${id}`}
        />
        {isChanged && this.renderActionButtons()}
      </RowBottom>
    );
  }

  renderActionButtons = (): React.ReactNode => {
    return (
      <Row>
        <LinkButton onClick={this.onSave}>{translate('save')}</LinkButton>
        <LinkButton onClick={this.onSaveAs}>{translate('save as')}</LinkButton>
      </Row>
    );
  }

  onChange = (event: any): void => {
    const {target: {value}} = event;
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
