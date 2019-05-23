import {omit} from 'lodash';
import * as React from 'react';
import {Overwrite} from 'utility-types';
import {routes} from '../../app/routes';
import {absoluteUrlFromPath, slugOfHostname} from '../../helpers/urlFactory';
import {firstUpperTranslated} from '../../services/translationService';
import {
  noOrganisation,
  noOrganisationId,
  Organisation
} from '../../state/domain-models/organisation/organisationModels';
import {UserSelection} from '../../state/user-selection/userSelectionModels';
import {CallbackAny, IdNamed, uuid} from '../../types/Types';
import {ButtonSave} from '../buttons/ButtonSave';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import {SelectFieldInput} from '../inputs/InputSelectable';
import {TextFieldInput} from '../inputs/TextFieldInput';
import {Column} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {Link, LinkProps} from '../links/Link';

const organisationById = (organisationId: uuid, organisations: Organisation[]): Organisation =>
  organisationId === noOrganisationId
    ? noOrganisation()
    : organisations.find(({id}) => id === organisationId)!;

const selectionOption = ({id, name}: UserSelection): IdNamed => ({id, name});

interface Props {
  addOrganisation: CallbackAny;
  addSubOrganisation: CallbackAny;
  organisation?: Organisation;
  organisations: Organisation[];
  selections: UserSelection[];
  selectionId?: uuid;
  updateOrganisation: CallbackAny;
}

type State = Overwrite<Organisation, {id?: uuid, slug?: string}>;

const StyledLink = withCssStyles(({cssStyles: {primary}, ...linkProps}: LinkProps & ThemeContext) =>
  <Link {...linkProps} style={{color: primary.bg}}/>);

export class OrganisationEditForm extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {name: '', selectionId: undefined, ...props.organisation};
  }

  componentWillReceiveProps({organisation}: Props) {
    if (organisation) {
      this.setState({...organisation});
    }
  }

  render() {
    const {parent, name, selectionId, slug, id} = this.state;
    const {organisations, selections} = this.props;

    const nameLabel = firstUpperTranslated('organisation name');
    const parentLabel = firstUpperTranslated('parent organisation');
    const selectionLabel = firstUpperTranslated('selection');

    const parentId: uuid = parent ? parent.id : noOrganisationId;

    const parentOrganisationOptions: Organisation[] = [
      noOrganisation(),
      ...organisations
        .filter((organisation: Organisation) => !organisation.parent)
    ];

    const selectionChooser = parent && parent.id !== noOrganisationId
      ? (() => {
        const currentUserOwnsSelectedSelection: UserSelection | undefined = selections
          .find((selection: UserSelection) => selectionId === selection.id);

        const selectionOptions: IdNamed[] = [
          {id: -1, name: ''},
          ...selections
            .filter((selection: UserSelection) =>
              currentUserOwnsSelectedSelection === undefined || currentUserOwnsSelectedSelection.id === selection.id
            )
            .map(selectionOption)
        ];

        return (
          <SelectFieldInput
            options={selectionOptions}
            floatingLabelText={selectionLabel}
            hintText={selectionLabel}
            id="selectionId"
            multiple={false}
            onChange={this.changeSelection}
            value={selectionId || selectionOptions[0].id}
          />
        );
      })()
      : null;

    // we assume that super admins never use custom domains to administrate other organisations,
    // if they do, this URL not match the expected behavior
    const loginUrl = slugOfHostname(window.location.hostname)
      .map((_) => absoluteUrlFromPath(`/#${routes.login}/`))
      .orElseGet(() => absoluteUrlFromPath(`/#${routes.login}/${slug}`));

    const customUrl = (
      <>
        <h3 style={{marginBottom: 16}}>{firstUpperTranslated('custom login URL')}</h3>
        <StyledLink to={loginUrl} target="_blank" className="underline">{loginUrl}</StyledLink>
      </>
    );

    return (
      <Column className="flex-fill-horizontally" style={{marginBottom: 24}}>
        <form onSubmit={this.wrappedSubmit}>
          <Row className="configuration-section">
            <Column className="one-third">
              <h2>{firstUpperTranslated('information')}</h2>
            </Column>
            <Column className="two-thirds">
              <TextFieldInput
                autoComplete="off"
                floatingLabelText={nameLabel}
                hintText={nameLabel}
                id="name"
                value={name}
                onChange={this.onChangeName}
              />
              {id && customUrl}
            </Column>
          </Row>
          <Row className="configuration-section">
            <Column className="one-third">
              <h2>{firstUpperTranslated('parent organisation')}</h2>
            </Column>
            <Column className="two-thirds">
              <SelectFieldInput
                options={parentOrganisationOptions}
                floatingLabelText={parentLabel}
                hintText={parentLabel}
                id="parent"
                multiple={false}
                onChange={this.changeParent}
                value={parentId}
              />

              {selectionChooser}

              <ButtonSave className="ButtonSave" type="submit"/>
            </Column>
          </Row>
        </form>
      </Column>
    );
  }

  changeParent = (_, __, value) => this.setState({parent: organisationById(value, this.props.organisations)});

  changeSelection = (_, __, value) => this.setState({selectionId: value});

  onChangeName = (event) => this.setState({name: event.target.value});

  wrappedSubmit = (event) => {
    event.preventDefault();

    const {addOrganisation, addSubOrganisation, updateOrganisation} = this.props;
    const {id, name, parent, selectionId} = this.state;
    const slug = name;
    const organisationWithoutParent = {...omit(this.state, 'parent'), slug};

    if (id) {
      updateOrganisation(parent ? {...this.state, slug} : organisationWithoutParent);
    } else {
      const parentId: uuid | undefined = parent ? parent.id : undefined;
      if (parentId && parentId !== noOrganisationId) {
        if (selectionId && selectionId !== -1) {
          addSubOrganisation(organisationWithoutParent, parentId);
        }
      } else {
        addOrganisation(organisationWithoutParent);
      }
    }
  }
}
