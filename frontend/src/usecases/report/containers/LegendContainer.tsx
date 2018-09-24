import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {ButtonDelete} from '../../../components/buttons/ButtonDelete';
import {ButtonVisibility} from '../../../components/buttons/ButtonVisibility';
import {IconIndicator} from '../../../components/icons/IconIndicator';
import {Row} from '../../../components/layouts/row/Row';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {orUnknown} from '../../../helpers/translations';
import {translate} from '../../../services/translationService';
import {Normalized} from '../../../state/domain-models/domainModels';
import {SelectionTreeEntities} from '../../../state/selection-tree/selectionTreeModels';
import {selectedListItemsToLegendTable} from '../../../state/ui/graph/measurement/helpers/graphContentsToLegendTable';
import {OnClick, OnClickWithId, uuid} from '../../../types/Types';
import {toggleSingleEntry} from '../reportActions';
import {GraphContents, LegendItem} from '../reportModels';
import './LegendContainer.scss';

interface OwnProps {
  graphContents: GraphContents;
  onToggleLine: OnClick;
  selectedListItems: uuid[];
  selectionTreeEntities: SelectionTreeEntities;
}

interface DispatchToProps {
  toggleSingleEntry: OnClickWithId;
}

export type LegendProps = OwnProps & DispatchToProps;

const style: React.CSSProperties = {
  display: 'table',
  width: '24px',
  height: '24px',
};

const renderFacility = ({facility}: LegendItem) => facility ? orUnknown(facility) : '';
const renderAddress = ({address}: LegendItem) => address ? orUnknown(address) : '';
const renderCity = ({city}: LegendItem) => city ? orUnknown(city) : '';
const renderMedium = ({medium}: LegendItem) =>
  Array.isArray(medium)
    ? medium.map((singleMedium) => (
      <IconIndicator
        key={singleMedium}
        medium={singleMedium}
        style={style}
      />
    ))
    : <IconIndicator medium={medium} style={style}/>;

class LegendComponent extends React.Component<LegendProps> {

  render() {
    const {
      onToggleLine,
      // graphContents,
      toggleSingleEntry,
      selectedListItems,
      selectionTreeEntities,
    } = this.props;

    // const {result, entities}: Normalized<LegendItem> = graphContentsToLegendTable(graphContents);
    // TODO do we want to construct the legend in a selector instead?
    const {result, entities}: Normalized<LegendItem> = selectedListItemsToLegendTable({
      selectedListItems,
      entities: selectionTreeEntities,
    });

    const renderVisibilityButton = ({id}: LegendItem) =>
      <ButtonVisibility onClick={onToggleLine} id={id}/>;

    const renderDeleteButton = ({id}: LegendItem) =>
      <ButtonDelete onClick={toggleSingleEntry} id={id}/>;

    return (
      <Row className="LegendContainer">
        <Table result={result} entities={entities.lines}>
          <TableColumn
            header={<TableHead className="first">{translate('facility')}</TableHead>}
            cellClassName={'first first-uppercase'}
            renderCell={renderFacility}
          />
          <TableColumn
            header={<TableHead>{translate('city')}</TableHead>}
            cellClassName={'first-uppercase'}
            renderCell={renderCity}
          />
          <TableColumn
            header={<TableHead>{translate('address')}</TableHead>}
            cellClassName={'first-uppercase'}
            renderCell={renderAddress}
          />
          <TableColumn
            header={<TableHead>{translate('medium')}</TableHead>}
            cellClassName={'icon'}
            renderCell={renderMedium}
          />
          <TableColumn
            header={<TableHead className="icon"/>}
            cellClassName="icon"
            renderCell={renderVisibilityButton}
          />
          <TableColumn
            header={<TableHead className="icon"/>}
            cellClassName="icon"
            renderCell={renderDeleteButton}
          />
        </Table>
      </Row>
    );
  }
}

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  toggleSingleEntry,
}, dispatch);

export const LegendContainer = connect<{}, DispatchToProps, OwnProps>(null, mapDispatchToProps)(LegendComponent);
