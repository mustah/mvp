import * as React from 'react';
import {Pagination} from '../../../collection/models/Collections';
import {Row} from '../layouts/row/Row';
import {Bold} from '../texts/Texts';
import './PaginationControl.scss';

interface PaginationControlProps {
  pagination: Pagination;
  nrOfEntities: number;
  changePage: (page) => any;
}

export const PaginationControl = (props: PaginationControlProps) => {
  const {pagination: {page, limit}, changePage, nrOfEntities} = props;
  const pages = Math.floor(nrOfEntities / limit);
  const changePageNext = () => changePage(page + 1);
  const changePagePrev = () => changePage(page - 1);
  const prevPage = page === 1 ? null : (
    <div onClick={changePagePrev} className="PaginationControl-button clickable">
      Previous page
    </div>
  );
  const nextPage = (page >= pages) ? null : (
    <div onClick={changePageNext} className="PaginationControl-button clickable">
      Next page
    </div>
  );
  return (
    <Row className="PaginationControl">
      {prevPage}
      <div>
        <Bold>{page} / {pages}</Bold>
      </div>
      {nextPage}
    </Row>
  );
};
