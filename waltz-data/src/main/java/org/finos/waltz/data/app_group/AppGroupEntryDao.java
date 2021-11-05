/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.data.app_group;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.app_group.AppGroupEntry;
import org.finos.waltz.model.app_group.ImmutableAppGroupEntry;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ApplicationGroupEntry.APPLICATION_GROUP_ENTRY;
import static org.finos.waltz.data.application.ApplicationDao.IS_ACTIVE;


@Repository
public class AppGroupEntryDao {

    private static final RecordMapper<Record, AppGroupEntry> appRefMapper = r ->
            ImmutableAppGroupEntry.builder()
                    .kind(EntityKind.APPLICATION)
                    .id(r.getValue(APPLICATION.ID))
                    .name(r.getValue(APPLICATION.NAME))
                    .provenance(r.getValue(APPLICATION_GROUP_ENTRY.PROVENANCE))
                    .isReadOnly(r.getValue(APPLICATION_GROUP_ENTRY.IS_READONLY))
                    .build();

    private final DSLContext dsl;


    @Autowired
    public AppGroupEntryDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<AppGroupEntry> findEntriesForGroup(long groupId) {
        return dsl
                .select(APPLICATION.ID, APPLICATION.NAME)
                .select(APPLICATION_GROUP_ENTRY.IS_READONLY, APPLICATION_GROUP_ENTRY.PROVENANCE)
                .from(APPLICATION)
                .innerJoin(APPLICATION_GROUP_ENTRY).on(APPLICATION_GROUP_ENTRY.APPLICATION_ID.eq(APPLICATION.ID))
                .where(APPLICATION_GROUP_ENTRY.GROUP_ID.eq(groupId))
                .and(IS_ACTIVE)
                .fetch(appRefMapper);
    }

    public int addApplication(long groupId, long applicationId) {
        return dsl.insertInto(APPLICATION_GROUP_ENTRY)
                .set(APPLICATION_GROUP_ENTRY.GROUP_ID, groupId)
                .set(APPLICATION_GROUP_ENTRY.APPLICATION_ID, applicationId)
                .onDuplicateKeyIgnore()
                .execute();
    }


    public int[] addApplications(long groupId, Collection<Long> applicationIds) {
        Query[] queries = applicationIds
                .stream()
                .map(id -> DSL.insertInto(APPLICATION_GROUP_ENTRY)
                        .set(APPLICATION_GROUP_ENTRY.GROUP_ID, groupId)
                        .set(APPLICATION_GROUP_ENTRY.APPLICATION_ID, id)
                        .onDuplicateKeyIgnore())
                .toArray(Query[]::new);
        return dsl.batch(queries).execute();
    }


    public int removeApplication(long groupId, long applicationId) {
        return dsl.delete(APPLICATION_GROUP_ENTRY)
                .where(APPLICATION_GROUP_ENTRY.GROUP_ID.eq(groupId))
                .and(APPLICATION_GROUP_ENTRY.APPLICATION_ID.eq(applicationId)
                        .and(APPLICATION_GROUP_ENTRY.IS_READONLY.isFalse()))
                .execute();
    }


    public int removeApplications(long groupId, List<Long> applicationIds) {
        return dsl.delete(APPLICATION_GROUP_ENTRY)
                .where(APPLICATION_GROUP_ENTRY.GROUP_ID.eq(groupId))
                .and(APPLICATION_GROUP_ENTRY.APPLICATION_ID.in(applicationIds)
                        .and(APPLICATION_GROUP_ENTRY.IS_READONLY.isFalse()))
                .execute();
    }
}
