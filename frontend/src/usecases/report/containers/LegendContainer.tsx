import {normalize, schema} from 'normalizr';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {ButtonDelete} from '../../../components/buttons/ButtonDelete';
import {ButtonVisibility} from '../../../components/buttons/ButtonVisibility';
import {Row} from '../../../components/layouts/row/Row';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {orUnknown} from '../../../helpers/translations';
import {translate} from '../../../services/translationService';
import {Normalized} from '../../../state/domain-models/domainModels';
import {OnClick, OnClickWithId, uuid} from '../../../types/Types';
import {toggleSingleEntry} from '../reportActions';
import {GraphContents, LegendItem} from '../reportModels';
import './LegendContainer.scss';

interface OwnProps {
  graphContents: GraphContents;
  onToggleLine: OnClick;
}

interface DispatchToProps {
  toggleSingleEntry: OnClickWithId;
}

type Props = OwnProps & DispatchToProps;

const renderName = (line: LegendItem) => orUnknown(line.label);
const renderAddress = (line: LegendItem) => orUnknown(line.address);
const renderCity = (line: LegendItem) => orUnknown(line.city);

class LegendComponent extends React.Component<Props> {
  render() {
    const {onToggleLine, graphContents, toggleSingleEntry} = this.props;

    const lines: Map<uuid, LegendItem> = new Map<uuid, LegendItem>();

    for (const line of graphContents.lines) {
      const legendItem: LegendItem = {
        label: line.name,
        address: line.address,
        city: line.city,
        color: '', // TODO a meters lines, should be identifiable by color.
        id: line.id,
      };

      if (!lines.has(legendItem.id) && !legendItem.label.startsWith('average')) { // TODO remove average condition
        lines.set(legendItem.id, legendItem);
      }
    }

    const lineSchema = [new schema.Entity('lines', {}, {idAttribute: 'id'})];
    const normalized: Normalized<LegendItem> = normalize(Array.from(lines.values()), lineSchema);

    const renderVisibilityButton = (line: LegendItem) =>
      <ButtonVisibility onClick={onToggleLine} id={line.id}/>;
    const renderDeleteButton = (line: LegendItem) =>
      <ButtonDelete onClick={toggleSingleEntry} id={line.id}/>;

    return (
      <Row>
        <Table result={normalized.result} entities={normalized.entities.lines}>
          <TableColumn
            header={<TableHead className="icon" />}
            renderCell={renderDeleteButton}
          />
          <TableColumn
            header={<TableHead className="icon" />}
            renderCell={renderVisibilityButton}
          />
          <TableColumn
            header={<TableHead>{translate('facility')}</TableHead>}
            cellClassName={'first-uppercase'}
            renderCell={renderName}
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
        </Table>
      </Row>
    );
  }
}

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  toggleSingleEntry,
}, dispatch);

export const LegendContainer = connect<{}, DispatchToProps, OwnProps>(null, mapDispatchToProps)(
  LegendComponent);
