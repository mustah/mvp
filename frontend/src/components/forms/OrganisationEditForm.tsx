import * as React from 'react';
import {Overwrite} from 'react-redux-typescript';
import {firstUpperTranslated} from '../../services/translationService';
import {Organisation} from '../../state/domain-models/organisation/organisationModels';
import {uuid} from '../../types/Types';
import {ButtonSave} from '../buttons/ButtonSave';
import {TextFieldInput} from '../inputs/InputText';
import {Column} from '../layouts/column/Column';
import './OrganisationEditForm.scss';

interface OrganisationFormProps {
  onSubmit: (event: any) => void;
  organisation?: Organisation;
}

type State = Overwrite<Organisation, {id?: uuid}>;

export class OrganisationEditForm extends React.Component<OrganisationFormProps, State> {

  constructor(props: OrganisationFormProps) {
    super(props);
    this.state = props.organisation ? {...props.organisation} : {name: '', slug: ''};
  }

  componentWillReceiveProps({organisation}: OrganisationFormProps) {
    if (organisation) {
      this.setState({...organisation});
    }
  }

  // TODO: need check that slug can't contain whitespaces or other characters that aren't allowed
  // in a url. Also need to be unique
  render() {
    const {name, slug} = this.state;

    const nameLabel = firstUpperTranslated('organisation name');
    const codeLabel = firstUpperTranslated('organisation slug');

    return (
      <form onSubmit={this.wrappedSubmit}>
        <Column className="EditOrganisationContainer">
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
            id="slug"
            value={slug.toString()}
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

  onChange = (event) => this.setState({[event.target.id]: event.target.value});

  wrappedSubmit = (event) => {
    event.preventDefault();
    this.props.onSubmit(this.state);
  }
}
