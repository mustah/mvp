import * as React from 'react';
import {ButtonDelete} from '../../../components/buttons/ButtonDelete';
import {ButtonVisibility} from '../../../components/buttons/ButtonVisibility';
import {IconIndicator} from '../../../components/icons/IconIndicator';
import {Medium} from '../../../components/indicators/indicatorWidgetModels';
import {Row} from '../../../components/layouts/row/Row';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {orUnknown} from '../../../helpers/translations';
import {translate} from '../../../services/translationService';
import {Normalized} from '../../../state/domain-models/domainModels';
import {SelectionTreeEntities} from '../../../state/selection-tree/selectionTreeModels';
import {selectedListItemsToLegendTable} from '../../../state/ui/graph/measurement/helpers/graphContentsToLegendTable';
import {OnClick, OnClickWithId, uuid} from '../../../types/Types';
import {GraphContents, LegendItem} from '../reportModels';
import './Legend.scss';

export interface LegendProps {
  graphContents: GraphContents;
  onToggleLine: OnClick;
  selectedListItems: uuid[];
  selectionTreeEntities: SelectionTreeEntities;
  toggleSingleEntry: OnClickWithId;
}

const iconIndicatorStyle: React.CSSProperties = {
  display: 'table',
  width: 24,
  height: 24,
};

const renderFacility = ({facility}: LegendItem) => facility ? orUnknown(facility) : '';
const renderAddress = ({address}: LegendItem) => address ? orUnknown(address) : '';
const renderCity = ({city}: LegendItem) => city ? orUnknown(city) : '';
const renderMedium = ({medium}: LegendItem) =>
  Array.isArray(medium)
    ? medium.map((singleMedium: Medium) => (
      <IconIndicator
        key={singleMedium}
        medium={singleMedium}
        style={iconIndicatorStyle}
      />
    ))
    : <IconIndicator medium={medium} style={iconIndicatorStyle}/>;

export const Legend = ({
  onToggleLine,
  toggleSingleEntry,
  selectedListItems,
  selectionTreeEntities,
}: LegendProps) => {
  // const {result, entities}: Normalized<LegendItem> = graphContentsToLegendTable(graphContents);
  // TODO do we want to construct the legend in a selector instead?
  const {result, entities}: Normalized<LegendItem> = selectedListItemsToLegendTable({
    selectedListItems,
    entities: selectionTreeEntities,
  });

  const renderVisibilityButton = ({id}: LegendItem) => <ButtonVisibility onClick={onToggleLine} id={id}/>;

  const renderDeleteButton = ({id}: LegendItem) => <ButtonDelete onClick={toggleSingleEntry} id={id}/>;

  return (
    <Row className="Legend">
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
};
