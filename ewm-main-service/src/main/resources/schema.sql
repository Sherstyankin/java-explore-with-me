CREATE TABLE IF NOT EXISTS users (
        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
        email varchar(254) UNIQUE NOT NULL,
        user_name varchar(250) NOT NULL
);

CREATE TABLE IF NOT EXISTS categories (
        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
        category_name varchar(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS locations (
        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
        lat double precision NOT NULL,
        lon double precision NOT NULL
);

CREATE TABLE IF NOT EXISTS events (
        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
        annotation varchar(2000) NOT NULL,
        category_id BIGINT NOT NULL,
        created_on timestamp WITHOUT TIME ZONE NOT NULL,
        description varchar(7000) NOT NULL,
        event_date timestamp WITHOUT TIME ZONE NOT NULL,
        user_id BIGINT NOT NULL,
        location_id BIGINT NOT NULL,
        paid boolean NOT NULL,
        participant_limit integer NOT NULL,
        published_on timestamp WITHOUT TIME ZONE, --edit by admin only
        request_moderation boolean NOT NULL,
        state varchar(20) NOT NULL,
        title varchar(120) NOT NULL,
        CONSTRAINT fk_events_to_categories FOREIGN KEY(category_id) REFERENCES categories(id) ON delete CASCADE ON update CASCADE,
        CONSTRAINT fk_events_to_users FOREIGN KEY(user_id) REFERENCES users(id) ON delete CASCADE ON update CASCADE,
        CONSTRAINT fk_events_to_locations FOREIGN KEY(location_id) REFERENCES locations(id) ON delete CASCADE ON update CASCADE
);

CREATE TABLE IF NOT EXISTS compilations (
        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
        pinned boolean NOT NULL,
        title varchar(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS event_compilations (
	compilation_id BIGINT,
	event_id BIGINT,
	CONSTRAINT event_compilations_pk PRIMARY KEY (compilation_id, event_id),
	CONSTRAINT compilation_id_fk FOREIGN KEY (compilation_id) REFERENCES compilations(id) ON delete CASCADE ON update CASCADE,
	CONSTRAINT event_id_fk FOREIGN KEY (event_id) REFERENCES events(id) ON delete CASCADE ON update CASCADE
);

CREATE TABLE IF NOT EXISTS requests (
        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
        created_on timestamp WITHOUT TIME ZONE NOT NULL,
        event_id BIGINT NOT NULL,
        requester_id BIGINT NOT NULL,
        status varchar(20) NOT NULL,
        CONSTRAINT uq_request UNIQUE (event_id, requester_id),
        CONSTRAINT fk_requests_to_users FOREIGN KEY(requester_id) REFERENCES users(id) ON delete CASCADE ON update CASCADE,
        CONSTRAINT fk_requests_to_events FOREIGN KEY(event_id) REFERENCES events(id) ON delete CASCADE ON update CASCADE
);

CREATE TABLE IF NOT EXISTS comments (
        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
        text varchar(2000) NOT NULL,
        event_id BIGINT NOT NULL,
        author_id BIGINT NOT NULL,
        created_on timestamp WITHOUT TIME ZONE NOT NULL,
        published_on timestamp WITHOUT TIME ZONE,
        state varchar(20) NOT NULL,
        CONSTRAINT fk_comments_to_events FOREIGN KEY(event_id) REFERENCES events(id),
        CONSTRAINT fk_comments_to_users FOREIGN KEY(author_id) REFERENCES users(id)
);









