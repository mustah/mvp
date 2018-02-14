import * as React from 'react';
import {firstUpperTranslated} from '../../services/translationService';
import {Organisation} from '../../state/domain-models/user/userModels';
import {uuid} from '../../types/Types';
import {ButtonSave} from '../buttons/ButtonSave';
import {TextFieldInput} from '../inputs/InputText';
import {Column} from '../layouts/column/Column';
import './UserEditForm.scss';

interface OrganisationFormProps {
  onSubmit: (event: any) => void;
  organisation?: Organisation;
}

interface State {
  id?: uuid;
  name: string;
  code: uuid;
}

export class OrganisationEditForm extends React.Component<OrganisationFormProps, State> {

  constructor(props: OrganisationFormProps) {
    super(props);
    if (props.organisation) {
      this.state = {...props.organisation};
    } else {
      this.state = {
        name: '',
        code: '',
      };
    }
  }

  componentWillReceiveProps({organisation}: OrganisationFormProps) {
    if (organisation) {
      this.setState({...organisation});
    }
  }

  onChange = (event) => this.setState({[event.target.id]: event.target.value});

  wrappedSubmit = (event) => {
    event.preventDefault();
    this.props.onSubmit(this.state);
  }

  // TODO: need check that code can't contain whitespaces or other characters that aren't allowed in a url.
  // Also need to be unique
  render() {
    const {name, code} = this.state;

    const nameLabel = firstUpperTranslated('organisation name');
    const codeLabel = firstUpperTranslated('organisation code');

    return (
      <form onSubmit={this.wrappedSubmit}>
        <Column className="EditUserContainer">
          <TextFieldInput
            floatingLabelText={nameLabel}
            hintText={nameLabel}
            id="name"
            value={name}
            onChange={this.onChange}
          />
          <TextFieldInput
            floatingLabelText={codeLabel}
            hintText={codeLabel}
            id="code"
            value={code.toString()}
            onChange={this.onChange}
          />
          <ButtonSave
            className="SaveButton"
            type="submit"
          />
        </Column>
      </form>
    );
  }
}
